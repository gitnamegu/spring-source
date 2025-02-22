package com.dowait.third;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BConfig {

    public BConfig() {
        System.out.println("创建BConfig的bean");
    }

    @Bean
    public CServiceImpl cService() {
        return new CServiceImpl();
    }

}
