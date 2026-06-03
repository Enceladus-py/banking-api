package com.example.demo.common.infrastructure.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Aspect
@Configuration
public class TransactionAopConfig {

	@Bean
	public Advisor useCaseTransactionAdvisor(TransactionManager transactionManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		// Applies to any method inside a class annotated with @TransactionalUseCase
		pointcut.setExpression("@within(com.example.demo.common.application.annotation.TransactionalUseCase)");

		TransactionInterceptor interceptor = new TransactionInterceptor(transactionManager,
				new MatchAlwaysTransactionAttributeSource());

		return new DefaultPointcutAdvisor(pointcut, interceptor);
	}
}
