package com.dowait.springSource.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeanDefinition {

    private Class clazz;
    private String scope;

}
