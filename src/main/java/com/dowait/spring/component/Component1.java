package com.dowait.spring.component;

import com.dowait.spring.bean.ComDemo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import(MineImportBeanDefinitionRegistrar.class)
public class Component1 {

    @Bean
    public ComDemo comDemo() {
        return new ComDemo();
    }

}
