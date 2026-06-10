package com.fintech.banking.coreapi.common.infrastructure.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * AOP Configuration that dynamically applies declarative transaction management
 * to any bean whose class is annotated with
 * {@link com.fintech.banking.coreapi.common.application.annotation.TransactionalUseCase}.
 */
@Aspect
@Configuration
public class TransactionAopConfig {

	/**
	 * Default constructor.
	 */
	public TransactionAopConfig() {
	}

	/**
	 * Creates an Advisor bean that matches methods in classes annotated with
	 * TransactionalUseCase and intercept them with a transaction interceptor.
	 *
	 * @param transactionManager
	 *            the transaction manager
	 * @return the transaction advisor
	 */
	@Bean
	public Advisor useCaseTransactionAdvisor(TransactionManager transactionManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		// Applies to any method inside a class annotated with @TransactionalUseCase
		pointcut.setExpression(
				"@within(com.fintech.banking.coreapi.common.application.annotation.TransactionalUseCase)");

		TransactionInterceptor interceptor = new TransactionInterceptor(transactionManager,
				new MatchAlwaysTransactionAttributeSource());

		return new DefaultPointcutAdvisor(pointcut, interceptor);
	}
}
