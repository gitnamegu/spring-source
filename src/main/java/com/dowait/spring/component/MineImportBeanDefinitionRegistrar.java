package com.dowait.spring.component;

import com.dowait.spring.third.User;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

public class MineImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    public MineImportBeanDefinitionRegistrar() {
        System.out.println("====");
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry);
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(User.class);
        // 将beanDefinition注册到容器
        registry.registerBeanDefinition("user5", beanDefinition);
        //User bean = applicationContext.getBean(User.class);
    }


}
