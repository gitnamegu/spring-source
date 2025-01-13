package com.dowait.springSource.processor;

import com.dowait.springSource.anno.MineComponent;
import com.dowait.springSource.service.impl.UserServiceImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 创建任何一个bean的过程中，都会执行BeanPostProcessor中的方法
 *
 * 程序员自定义实现，甚至可以不用返回传入的对象，而是返回其他对象。AOP就是通过这种机制实现的。
 */
@MineComponent("businessPostProcessor")
public class BusinessPostProcessorMine implements MineBeanPostProcessor {

    /**
     * bean初始化前的处理
     * @param bean
     * @param beanName
     * @return
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 假设需求是对userService特殊处理
        if (beanName.equals("userService")) {
            UserServiceImpl userService = (UserServiceImpl) bean;
            System.out.println("对userService的处理");
        }
        return bean;
    }

    /**
     * bean初始化完成后的处理
     *
     * 实现AOP
     * @param bean
     * @param beanName
     * @return
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            // JDK的动态代理生成UserService的代理对象
            Object proxyInstance = Proxy.newProxyInstance(BusinessPostProcessorMine.class.getClassLoader(),
                    bean.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            System.out.println("这是代理的逻辑，可以自定义实现，比如可以开启事务");
                            // 通过反射的方式执行bean的方法，也就是执行原始方法的业务逻辑
                            return method.invoke(bean, args);
                        }
                    });
            return proxyInstance;
        }

        return bean;
    }

}
