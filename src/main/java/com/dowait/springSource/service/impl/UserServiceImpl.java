package com.dowait.springSource.service.impl;

import com.dowait.springSource.anno.MineAutowired;
import com.dowait.springSource.anno.MineComponent;
import com.dowait.springSource.springInterface.BeanNameAware;
import com.dowait.springSource.springInterface.InitializingBean;

@MineComponent("userService")
//@Scope("prototype")
public class UserServiceImpl implements UserService, BeanNameAware, InitializingBean {

    @MineAutowired
    private OrderServiceImpl orderService;

    @Override
    public void test() {
        System.out.println("userService, test方法, 被执行");
    }

    /**
     * aware回调中的具体逻辑由我们实现
     * @param beanName
     */
    @Override
    public void setBeanName(String beanName) {
        System.out.println("aware回调, 这个方法传入beanName");
    }

    /**
     * 初始化中的具体逻辑由我们实现
     */
    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化逻辑");
    }
}
