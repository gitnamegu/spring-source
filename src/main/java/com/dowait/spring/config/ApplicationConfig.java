package com.dowait.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

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
