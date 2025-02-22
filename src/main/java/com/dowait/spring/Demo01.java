package com.dowait.spring;

import com.dowait.spring.bean.ComDemo;
import com.dowait.spring.component.MineImportBeanDefinitionRegistrar;
import com.dowait.spring.service.RoleService;
import com.dowait.spring.third.DemoBeanDefinitionRegistry;
import com.dowait.spring.third.User;
import com.dowait.springSource.config.AppConfig;
import com.dowait.third.AServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

//@Import(DemoBeanDefinitionRegistry.class)
public class Demo01 {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.dowait.spring");



    }

}
