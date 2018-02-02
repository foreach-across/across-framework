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
package com.foreach.across.core.config;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

/**
 * Base configuration for a module {@link org.springframework.context.ApplicationContext}.
 *
 * @author Arne Vandamme
 */
@EnableConfigurationProperties
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonModuleConfiguration
{
	@Bean
	@ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Activate method validation in every module.
	 */
	@Configuration
	@ConditionalOnClass(ExecutableValidator.class)
	@ConditionalOnBean(Validator.class)
	@ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
	static class MethodValidationConfiguration
	{
		@Bean
		@ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
		public static MethodValidationPostProcessor methodValidationPostProcessor(
				Environment environment, @Lazy Validator validator ) {
			MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
			processor.setProxyTargetClass( determineProxyTargetClass( environment ) );
			processor.setValidator( validator );
			return processor;
		}

		private static boolean determineProxyTargetClass( Environment environment ) {
			Boolean value = environment.getProperty( "spring.aop.proxy-target-class", Boolean.class );
			return Boolean.TRUE.equals( value );
		}
	}
}
