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

package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.config.CommonModuleConfiguration;
import com.foreach.across.core.context.*;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.PropertyPlaceholderSupportConfigurer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.Collection;
import java.util.Collections;

public class AnnotationConfigBootstrapApplicationContextFactory implements BootstrapApplicationContextFactory
{
	@Override
	public AcrossConfigurableApplicationContext createApplicationContext() {
		return new AcrossApplicationContext();
	}

	@Override
	public AcrossConfigurableApplicationContext createInstallerContext() {
		AcrossApplicationContext installerContext = new AcrossApplicationContext();
		installerContext.setInstallerMode( true );
		installerContext.register( PropertyPlaceholderSupportConfigurer.Config.class );

		return installerContext;
	}

	/**
	 * Create the Spring ApplicationContext for the root of the AcrossContext.
	 * Optionally a parent ApplicationContext can be specified and a map of singletons that are guaranteed
	 * to be available when the ApplicationContext has been created.
	 *
	 * @param across                   AcrossContext being created.
	 * @param parentApplicationContext Parent ApplicationContext, can be null.
	 * @return Spring ApplicationContext instance implementing AcrossConfigurableApplicationContext.
	 */
	@Override
	public AcrossConfigurableApplicationContext createApplicationContext( AcrossContext across,
	                                                                      ApplicationContext parentApplicationContext ) {
		AcrossConfigurableApplicationContext applicationContext = createApplicationContext();
		applicationContext.setDisplayName( "[" + across.getId() + "]" );

		if ( parentApplicationContext != null ) {
			applicationContext.setParent( parentApplicationContext );
		}

		return applicationContext;
	}

	/**
	 * Create the Spring ApplicationContext for a particular AcrossModule.
	 *
	 * @param across                AcrossContext being loaded.
	 * @param moduleBootstrapConfig Bootstrap configuration of the AcrossModule being created.
	 * @param parentContext         Contains the parent context.
	 * @return Spring ApplicationContext instance implementing AcrossConfigurableApplicationContext.
	 */
	@Override
	public AcrossConfigurableApplicationContext createApplicationContext( AcrossContext across,
	                                                                      ModuleBootstrapConfig moduleBootstrapConfig,
	                                                                      AcrossApplicationContextHolder parentContext ) {
		AcrossConfigurableApplicationContext child = createApplicationContext();
		child.setDisplayName( moduleBootstrapConfig.getModuleName() );
		child.setParent( parentContext.getApplicationContext() );

		return child;
	}

	/**
	 * Loads beans and definitions in the root ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param context Contains the root Spring ApplicationContext.
	 */
	@Override
	public void loadApplicationContext( AcrossContext across, AcrossApplicationContextHolder context ) {
		AcrossConfigurableApplicationContext root = context.getApplicationContext();
		Collection<ApplicationContextConfigurer> configurers = AcrossContextUtils.getApplicationContextConfigurers( across );

		loadApplicationContext( root, configurers, Collections.emptyList() );
	}

	/**
	 * Loads beans and definitions in the module ApplicationContext.
	 *
	 * @param across                AcrossContext being loaded.
	 * @param moduleBootstrapConfig Bootstrap configuration of the AcrossModule being created.
	 * @param context               Contains the Spring ApplicationContext for the module.
	 */
	@Override
	public void loadApplicationContext( AcrossContext across,
	                                    ModuleBootstrapConfig moduleBootstrapConfig,
	                                    AcrossApplicationContextHolder context ) {
		AcrossConfigurableApplicationContext child = context.getApplicationContext();

		loadApplicationContext( child, moduleBootstrapConfig.getApplicationContextConfigurers(), moduleBootstrapConfig.getPreviouslyExposedBeans() );
	}

	@Override
	public void loadApplicationContext( AcrossConfigurableApplicationContext context,
	                                    Collection<ApplicationContextConfigurer> configurers,
	                                    Collection<ExposedModuleBeanRegistry> exposedBeanRegistries ) {
		ConfigurableEnvironment environment = context.getEnvironment();

		context.register( CommonModuleConfiguration.class );

		for ( ApplicationContextConfigurer configurer : configurers ) {
			// First register property sources
			PropertySources propertySources = configurer.propertySources();

			if ( propertySources != null ) {
				for ( PropertySource<?> propertySource : propertySources ) {
					// Lower configurers means precedence in property sources
					environment.getPropertySources().addFirst( propertySource );
				}
			}
		}

		for ( ApplicationContextConfigurer configurer : configurers ) {
			ProvidedBeansMap providedBeans = configurer.providedBeans();

			if ( providedBeans != null ) {
				context.provide( providedBeans );
			}

			for ( BeanFactoryPostProcessor postProcessor : configurer.postProcessors() ) {
				context.addBeanFactoryPostProcessor( postProcessor );
			}

			if ( !ArrayUtils.isEmpty( configurer.componentScanPackages() ) ) {
				context.scan( configurer.componentScanPackages(), configurer.excludedTypeFilters() );
			}

			if ( !ArrayUtils.isEmpty( configurer.annotatedClasses() ) ) {
				context.register( configurer.annotatedClasses() );
			}
		}

		context.provide( new ProvidedBeansMap( Collections.singletonMap( "exposedBeansPostProcessor", pp( exposedBeanRegistries ) ) ) );

		context.refresh();
		context.start();
	}

	private BeanDefinitionRegistryPostProcessor pp( Collection<ExposedModuleBeanRegistry> exposedBeanRegistries ) {
		return new BeanDefinitionRegistryPostProcessor()
		{
			@Override
			public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {

			}

			@Override
			public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
				exposedBeanRegistries.forEach( r -> r.copyTo( beanFactory, false ) );
			}
		};
	}
}
