/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
