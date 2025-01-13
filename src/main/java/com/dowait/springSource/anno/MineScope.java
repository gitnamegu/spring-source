package com.dowait.springSource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 代表Scope注解的生命周期，RUNTIME使jvm在运行的时候可以获取到Scope注解的信息
@Target(ElementType.TYPE)  // 代表Scope注解只能顶在类上边
public @interface MineScope {

    String value();

}
