package com.foreach.across.core.context.configurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configures transaction management support on all modules where it applies.
 */
public class TransactionManagementConfigurer extends AnnotatedClassConfigurer
{
	public TransactionManagementConfigurer() {
		super( Config.class );
	}

	@Configuration
	@EnableTransactionManagement
	public static class Config
	{

	}
}
