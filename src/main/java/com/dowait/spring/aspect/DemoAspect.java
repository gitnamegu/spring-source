package com.dowait.spring.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class DemoAspect {

    @Pointcut("execution(public void com.dowait.spring.service.impl.RoleServiceImpl.test())")
    public void pointcut1() {
    }

    @Pointcut("execution(public void com.dowait.spring.service.impl.RoleServiceImpl.test2())")
    public void pointcut2() {
    }

    @Before("pointcut1() || pointcut2()")
    public void before(JoinPoint joinPoint) {
        System.out.println("aspect before 切面方法被执行1");
    }


}
