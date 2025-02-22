package com.dowait.spring.config;

import com.dowait.spring.selectors.MineSelector;
import org.springframework.context.annotation.*;

@Configuration
@EnableAspectJAutoProxy
@Import(MineSelector.class)
public class ApplicationConfig {


}
