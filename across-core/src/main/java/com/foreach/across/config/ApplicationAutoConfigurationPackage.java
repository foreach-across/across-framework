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

import com.foreach.across.core.AcrossException;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Objects;

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
	public static final String FAKE_APPLICATION_PACKAGE = "should.only.match.application.package";

	@Getter(AccessLevel.PACKAGE)
	private String applicationModulePackage = "application";

	@Override
	public void registerBeanDefinitions( AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry ) {
		List<String> packages = AutoConfigurationPackages.get( (BeanFactory) registry );
		if ( !packages.isEmpty() && Objects.equals( FAKE_APPLICATION_PACKAGE, packages.get( 0 ) ) ) {
			String defaultPackage = ClassUtils.getPackageName( importingClassMetadata.getClassName() );
			applicationModulePackage = defaultPackage + ".application";
			LOG.info( "Disabling @AutoConfigurationPackage on the root package - Across applications support only the application module" );
			AcrossDynamicModulesConfiguration.verifyNoConflictingComponentScans( importingClassMetadata, defaultPackage );
		}
		else {
			throw new AcrossException( "Unsupported package configuration" );
		}

		SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
		if ( !singletonBeanRegistry.containsSingleton( ApplicationAutoConfigurationPackage.class.getName() ) ) {
			singletonBeanRegistry.registerSingleton( ApplicationAutoConfigurationPackage.class.getName(), this );
		}
	}

	public static boolean hasOnlyFakeApplicationPackage( BeanFactory registry ) {
		List<String> packages = AutoConfigurationPackages.get( registry );
		return !packages.isEmpty() && Objects.equals( FAKE_APPLICATION_PACKAGE, packages.get( 0 ) );
	}
}
