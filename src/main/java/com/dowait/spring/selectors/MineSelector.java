package com.dowait.spring.selectors;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 将第三方的包中的类创建为bean的案例
 */
public class MineSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // 使用方式1：直接创建AServiceImpl的bean，AServiceImpl是第三方包中的普通的类
        //return new String[]{"com.dowait.third.AServiceImpl"};

        // 使用方式2：BConfig是第三方包中的配置类，里边定义了CServiceImpl的bean。这个时候就会创建BConfig、CServiceImpl两个bean
        return new String[]{"com.dowait.third.BConfig"};
    }

}
