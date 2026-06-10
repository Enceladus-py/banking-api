package com.fintech.banking.coreapi.common.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.fintech.banking.coreapi.common.application.annotation.UseCase;

/**
 * Configuration class that sets up Component Scanning for classes annotated
 * with {@link UseCase}.
 */
@Configuration
@ComponentScan(basePackages = "com.fintech.banking.coreapi", includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
public class BeanConfig {

	/**
	 * Default constructor.
	 */
	public BeanConfig() {
	}
}
