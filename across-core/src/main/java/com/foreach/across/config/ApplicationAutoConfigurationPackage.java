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
package com.foreach.across.config;

import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Disables the auto-configuration package of the main application class,
 * and injects it in the application module instead.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
final class ApplicationAutoConfigurationPackage implements ImportBeanDefinitionRegistrar, AcrossBootstrapConfigurer
{
	private static final String BEAN = AutoConfigurationPackages.class.getName();

	@Getter(AccessLevel.PACKAGE)
	private String applicationModulePackage = "application";

	@Override
	public void registerBeanDefinitions( AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry ) {
		if ( !registry.containsBeanDefinition( BEAN ) ) {
			AutoConfigurationPackages.register( registry );
		}

		BeanDefinition beanDefinition = registry.getBeanDefinition( BEAN );
		ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();
		String[] existing = (String[]) constructorArguments.getIndexedArgumentValue( 0, String[].class ).getValue();

		if ( existing.length > 0 ) {
			applicationModulePackage = existing[0] + ".application";
			LOG.info( "Disabling @AutoConfigurationPackage on the root package - Across applications support only the application module" );
			constructorArguments.addIndexedArgumentValue( 0, new String[0] );

			AcrossDynamicModulesConfiguration.verifyNoConflictingComponentScans( importingClassMetadata, existing[0] );
		}

		if ( registry instanceof BeanFactory ) {
			LOG.trace( "Eager instantiation of AutoConfigurationPackages singleton" );
			AutoConfigurationPackages.get( (BeanFactory) registry );
		}

		SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
		if ( !singletonBeanRegistry.containsSingleton( ApplicationAutoConfigurationPackage.class.getName() ) ) {
			singletonBeanRegistry.registerSingleton( ApplicationAutoConfigurationPackage.class.getName(), this );
		}
	}
}
