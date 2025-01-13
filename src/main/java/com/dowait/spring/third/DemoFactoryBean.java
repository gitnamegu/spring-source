package com.dowait.spring.third;

import org.springframework.beans.factory.FactoryBean;

public class DemoFactoryBean implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        // 生成mapper的代理对象
        return new User();
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
    }

}
