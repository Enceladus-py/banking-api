package com.example.demo.common.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.example.demo.common.application.annotation.UseCase;

@Configuration
@ComponentScan(basePackages = "com.example.demo", includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
public class BeanConfig {
}
