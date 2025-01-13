package com.dowait.springSource.config;

import com.dowait.springSource.anno.MineComponentScan;

/**
 * 这个配置类中可以加@ComponentScan注解，同时也可以在这个配置类的方法上增加@Bean注解
 */
//@ComponentScan("com.dowait.springSource.service.impl")
@MineComponentScan("com.dowait.springSource")
public class AppConfig {
}
