package com.foreach.across.core.context.configurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configures transaction management support on all modules where it applies.
 */
public class TransactionManagementConfigurer extends AnnotatedClassConfigurer
{
	/**
	 * Order for the AOP interceptor.
	 */
	public static final int INTERCEPT_ORDER = Ordered.LOWEST_PRECEDENCE - 10;

	public TransactionManagementConfigurer() {
		super( Config.class );
	}

	@Configuration
	@EnableTransactionManagement(order = INTERCEPT_ORDER)
	public static class Config
	{

	}
}
