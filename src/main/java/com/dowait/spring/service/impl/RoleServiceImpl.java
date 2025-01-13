package com.dowait.spring.service.impl;

import com.dowait.spring.service.RoleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    @Override
    public void test() {
        System.out.println("RoleServiceImpl 的 test1 方法 被执行");
    }

    @Override
    public void test2() {
        System.out.println("RoleServiceImpl 的 test2 方法 被执行");
    }

}
