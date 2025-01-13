package com.dowait.springSource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 代表ComponentScan注解的生命周期，RUNTIME使jvm在运行的时候可以获取到ComponentScan注解的信息
@Target(ElementType.TYPE)  // 代表ComponentScan注解只能顶在类上边
public @interface MineComponentScan {

    /**
     * ComponentScan注解用来定义扫描路径，所以定义value属性，用来指定路径
     * @return
     */
    String value(); // 没有指定默认值

}
