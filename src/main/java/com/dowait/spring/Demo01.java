package com.dowait.spring;

import com.dowait.spring.bean.ComDemo;
import com.dowait.spring.component.MineImportBeanDefinitionRegistrar;
import com.dowait.spring.service.RoleService;
import com.dowait.spring.third.DemoBeanDefinitionRegistry;
import com.dowait.spring.third.User;
import com.dowait.springSource.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@Import(DemoBeanDefinitionRegistry.class)
public class Demo01 {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.dowait.spring");
        /*RoleService roleService = applicationContext.getBean(RoleService.class);
        roleService.test();

        roleService.test2();
        System.out.println("====");*/

        // BeanDefinition，用来定义一个bean的
        /*AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(User.class);
        // 将beanDefinition注册到容器
        applicationContext.registerBeanDefinition("user", beanDefinition);
        User bean = applicationContext.getBean(User.class);
        System.out.println(bean);*/

        // BeanDefinition，结合FactoryBean定义bean，相比上边的方式更灵活，可以在FactoryBean的getObject方法中灵活的写代码
        /*AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(DemoFactoryBean.class);
        // 支持给DemoFactoryBean的构造方法传参数
        //beanDefinition.getConstructorArgumentValues().addGenericArgumentValue();
        applicationContext.registerBeanDefinition("user", beanDefinition);
        User bean = applicationContext.getBean("user", User.class);
        DemoFactoryBean bean2 = applicationContext.getBean("&user", DemoFactoryBean.class);
        System.out.println(bean);
        System.out.println(bean2);*/

        /*User bean = applicationContext.getBean("user", User.class);
        System.out.println(bean);*/

        /*ComDemo comDemo = applicationContext.getBean("comDemo", ComDemo.class);
        System.out.println(comDemo);*/

        /*RoleService roleService = applicationContext.getBean(RoleService.class);
        roleService.test();*/

        User user5 = applicationContext.getBean("user5", User.class);
        System.out.println(user5);

        System.out.println(AutoProxyRegistrar.class.getName());


    }
}
