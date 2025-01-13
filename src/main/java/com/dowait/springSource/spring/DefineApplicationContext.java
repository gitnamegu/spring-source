package com.dowait.springSource.spring;

import cn.hutool.core.io.FileUtil;
import com.dowait.springSource.anno.MineAutowired;
import com.dowait.springSource.anno.MineComponent;
import com.dowait.springSource.anno.MineComponentScan;
import com.dowait.springSource.anno.MineScope;
import com.dowait.springSource.processor.MineBeanPostProcessor;
import com.dowait.springSource.springInterface.BeanNameAware;
import com.dowait.springSource.springInterface.InitializingBean;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟手写Spring容器
 * 容器类
 */
public class DefineApplicationContext {

    private Class configClass;

    /**
     * 单例池，存储单例对象，只有单例bean对象才放入这个map   key:beanName; value:Bean对象
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /**
     * BeanDefinition池   key:beanName; value:BeanDefinition
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private List<MineBeanPostProcessor> mineBeanPostProcessorList = new ArrayList<>();

    public DefineApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类   配置类上可能有@ComponentScan注解，配置类的方法上也可能有@Bean注解
        // Spring解析配置类的注解、配置类中方法的注解，Spring只解析自己提供的那些注解

        // 如果配置类上顶了@ComponentScan注解，确认扫描路径，然后要通过应用类加载器获取到扫描路径下的所有文件，因为类加载器可以获取到磁盘下的全路径
        MineComponentScan mineComponentScanAnnotation = (MineComponentScan) configClass.getDeclaredAnnotation(MineComponentScan.class);
        String scanPath = mineComponentScanAnnotation.value();
        // 根据scanPath获取包下的所有的类  依赖应用类加载器
        scanPath = scanPath.replace(".", "/");
        URL resource = ClassLoader.getSystemResource(scanPath);
        List<File> files = FileUtil.loopFiles(resource.getFile());
        // 通过classLoader加载路径下的类生成class对象
        ClassLoader classLoader = DefineApplicationContext.class.getClassLoader();

        // 遍历文件，将配置了spring注解的类的元信息放入beanDefinitionMap
        for (File file : files) {
            // file打印出来的路径是编译后的目录：/Users/dowait/code-repository/spring/target/classes/com/dowait/springSource/service
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".class")) {
                int index = fileName.indexOf(scanPath);
                String className = fileName.substring(index, fileName.indexOf(".class"));
                className = className.replace("/", ".");
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    // 扫描到bean的类
                    if (clazz.isAnnotationPresent(MineComponent.class)) {

                        // 判断这个类是否实现了BeanPostProcessor，如果是BeanPostProcessor子类，将其存入beanPostProcessorList，用于createBean创建bean的过程中做加工
                        if (MineBeanPostProcessor.class.isAssignableFrom(clazz)) {
                            MineBeanPostProcessor mineBeanPostProcessor = (MineBeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            mineBeanPostProcessorList.add(mineBeanPostProcessor);
                        }

                        // 解析这个类的元信息，放入beanDefinitionMap中，用于创建bean和获取bean
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);
                        MineComponent mineComponentAnnotation = clazz.getDeclaredAnnotation(MineComponent.class);
                        String beanName = mineComponentAnnotation.value();
                        if (clazz.isAnnotationPresent(MineScope.class)) {
                            MineScope mineScopeAnnotation = clazz.getDeclaredAnnotation(MineScope.class);
                            beanDefinition.setScope(mineScopeAnnotation.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

        // 单例bean，创建对象，放入单例池，应用方使用的时候直接根据bean的名字从单例池中拿对象（这里不考虑懒加载）
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            String scope = beanDefinition.getScope();
            if (StringUtils.pathEquals(scope, "singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
            // 这里不做原型bean的初始化，因为原型bean每次获取都是新的bean
        });

    }

    /**
     * 通过反射创建bean, 并实现依赖注入
     * @param beanDefinition
     * @return
     */
    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 要创建的bean的类的class对象
        Class clazz = beanDefinition.getClazz();
        try {
            // 调用无参的构造方法
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入，也叫注入依赖，也叫自动注入
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(MineAutowired.class)) {
                    // 根据属性的名字获取或创建所属类型的bean对象
                    String fieldName = declaredField.getName();
                    Object fieldBean = getBean(fieldName);
                    declaredField.setAccessible(true);
                    declaredField.set(instance, fieldBean);
                }
            }

            // 如果bean的类实现了BeanNameAware接口，aware回调
            if (instance instanceof BeanNameAware) {
                BeanNameAware aware = (BeanNameAware) instance;
                aware.setBeanName("abc");
            }

            // BeanPostProcessor，bean的处理器，是spring对外提供的扩展机制，跟第三方框架整合时，或者我们编写业务代码时，经常会用到。比如AOP就是通过BeanPostProcessor实现的
            // Spring获取到所有的BeanPostProcessor，执行顺序按照order值排序
            // 这个bean在初始化前，调用所有的beanPostProcessor做加工。有可能这些BeanPostProcessor返回的对象不是原来的对象，有可能是新的对象或者代理对象
            for (MineBeanPostProcessor mineBeanPostProcessor : mineBeanPostProcessorList) {
                instance = mineBeanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // bean初始化
            if (instance instanceof InitializingBean) {
                InitializingBean initializingBean = (InitializingBean) instance;
                initializingBean.afterPropertiesSet();
            }

            // 初始化后，调用所有的beanPostProcessor做加工。有可能这些BeanPostProcessor返回的对象不是原来的对象，有可能是新的对象或者代理对象
            // 实现AOP
            for (MineBeanPostProcessor mineBeanPostProcessor : mineBeanPostProcessorList) {
                instance = mineBeanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }




            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据bean的名字获取bean对象
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        // 单例bean
        if (beanDefinition.getScope().equals("singleton")) {
            return singletonObjects.get(beanName);
        } else {
            // 原型bean, 每次创建新的bean对象
            return createBean(beanName, beanDefinition);
        }
    }

}
