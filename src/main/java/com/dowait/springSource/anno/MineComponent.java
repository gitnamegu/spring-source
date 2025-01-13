package com.dowait.springSource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 代表Component注解的生命周期，RUNTIME使jvm在运行的时候可以获取到Component注解的信息
@Target(ElementType.TYPE)  // 代表Component注解只能顶在类上边
public @interface MineComponent {

    /**
     * Component注解用来定义bean，value用来指定bean的名字
     * @return
     */
    String value() default "";  // 默认值空字符串

}
