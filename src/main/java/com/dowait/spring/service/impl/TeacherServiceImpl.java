package com.dowait.spring.service.impl;

import com.dowait.spring.service.RoleService;
import com.dowait.spring.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private RoleService roleService11;

    @Override
    public void wa() {
        System.out.println("TeacherService中的roleService属性：" + roleService11);
    }
}
