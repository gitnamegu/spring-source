package com.dowait.spring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfig {

    /*@Bean
    public RoleService roleService() {
        RoleServiceImpl roleService = new RoleServiceImpl();
        System.out.println("ApplicationConfig中的roleService对象：" + roleService);
        return roleService;
    }*/

}
