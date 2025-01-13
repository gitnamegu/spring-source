package com.dowait.springSource.main;

import com.dowait.springSource.service.impl.UserService;
import com.dowait.springSource.spring.DefineApplicationContext;
import com.dowait.springSource.config.AppConfig;

public class Main01 {

    public static void main(String[] args) {
        // 模拟的是注解方式的源码
        DefineApplicationContext applicationContext = new DefineApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();

    }

}
