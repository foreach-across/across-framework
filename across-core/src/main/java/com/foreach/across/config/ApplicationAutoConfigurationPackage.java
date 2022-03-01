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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

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
		List<String> basePackages = getBasePackages( (AbstractBeanDefinition) beanDefinition );

		if ( !basePackages.isEmpty() ) {
			String basePackage = basePackages.get( 0 );
			applicationModulePackage = basePackage + ".application";
			LOG.info( "Disabling @AutoConfigurationPackage on the root package - Across applications support only the application module" );
			removePackage( beanDefinition, basePackage );

			AcrossDynamicModulesConfiguration.verifyNoConflictingComponentScans( importingClassMetadata, basePackage );
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

	/**
	 * Get Across base package from BeanDefinition
	 * Previously collected by constructor arguments, but this was changed in:
	 * {@link org.springframework.boot.autoconfigure.AutoConfigurationPackages#register(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.String...)}
	 *
	 * @param beanDefinition for which to return the base package names
	 * @return List of base package names
	 */
	private List<String> getBasePackages( AbstractBeanDefinition beanDefinition ) {
		try {
			Object instance = beanDefinition.getInstanceSupplier().get();
			Field packages = ReflectionUtils.findField( instance.getClass(), "packages" );
			ReflectionUtils.makeAccessible( packages );
			return (List<String>) packages.get( instance );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * Remove Across base package from BeanDefinition
	 * Previously done by adjusting the construct arguments, but this was changed in:
	 * {@link org.springframework.boot.autoconfigure.AutoConfigurationPackages#register(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.String...)}
	 *
	 * @param beanDefinition the bean definition from which to remove the package name
	 * @param packageToRemove the package name to be removed
	 */
	private void removePackage( BeanDefinition beanDefinition, String packageToRemove ) {
		try {
			Field field2 = ReflectionUtils.findField( beanDefinition.getClass(), "basePackages" );
			ReflectionUtils.makeAccessible( field2 );
			Set<String> o = (Set<String>) field2.get( beanDefinition );
			o.removeIf( existingPackage -> StringUtils.equals( existingPackage, packageToRemove ) );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}
}
