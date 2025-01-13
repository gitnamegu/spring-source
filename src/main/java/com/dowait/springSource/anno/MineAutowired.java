package com.dowait.springSource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 代表Autowired注解的生命周期，RUNTIME使jvm在运行的时候可以获取到Autowired注解的信息
@Target({ElementType.TYPE, ElementType.FIELD })  // 代表Autowired注解可以顶在类和方法上边。实际上只允许顶在属性上就可以
public @interface MineAutowired {

}
