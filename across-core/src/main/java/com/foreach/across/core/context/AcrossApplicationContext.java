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

package com.foreach.across.core.context;

import com.foreach.across.core.context.annotation.ModuleConfigurationBeanNameGenerator;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.support.MessageSourceBuilder;
import com.foreach.across.core.events.EventHandlerBeanPostProcessor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * ApplicationContext that allows a set of preregistered singletons to be passed in.
 */
public class AcrossApplicationContext extends AnnotationConfigApplicationContext implements AcrossConfigurableApplicationContext
{
	private boolean installerMode = false;

	public AcrossApplicationContext() {
		this( new AcrossListableBeanFactory() );
	}

	protected AcrossApplicationContext( AcrossListableBeanFactory beanFactory ) {
		super( beanFactory );
		setBeanNameGenerator( new ModuleConfigurationBeanNameGenerator() );
	}

	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardAcrossEnvironment();
	}

	/**
	 * Configure the context for installer mode.  Can only be done before context has started.
	 * This will configure a context with limited functionality.
	 *
	 * @param installerMode true if installer mode enabled
	 */
	public void setInstallerMode( boolean installerMode ) {
		this.installerMode = installerMode;
	}

	/**
	 * Adds a collection of provided beans to application context.
	 *
	 * @param beans One or more ProvidedBeansMaps to add.
	 */
	public void provide( ProvidedBeansMap... beans ) {
		for ( ProvidedBeansMap providedBeans : beans ) {
			for ( Map.Entry<String, BeanDefinition> definition : providedBeans.getBeanDefinitions().entrySet() ) {
				registerBeanDefinition( definition.getKey(), definition.getValue() );
			}
			for ( Map.Entry<String, Object> singleton : providedBeans.getSingletons().entrySet() ) {
				getBeanFactory().registerSingleton( singleton.getKey(), singleton.getValue() );
			}
		}
	}

	@Override
	protected void initMessageSource() {
		if ( !installerMode ) {
			new MessageSourceBuilder( getBeanFactory() ).build( getInternalParentMessageSource() );
		}

		super.initMessageSource();
	}

	@Override
	protected void registerBeanPostProcessors( ConfigurableListableBeanFactory beanFactory ) {
		super.registerBeanPostProcessors( beanFactory );

		registerEventHandlerBeanPostProcessor( beanFactory );

		// Set the conversion service on the environment as well
		ConfigurableEnvironment environment = getEnvironment();

		if ( beanFactory.containsBean( CONVERSION_SERVICE_BEAN_NAME )
				&& beanFactory.isTypeMatch( CONVERSION_SERVICE_BEAN_NAME, ConfigurableConversionService.class ) ) {
			environment.setConversionService(
					beanFactory.getBean( CONVERSION_SERVICE_BEAN_NAME, ConfigurableConversionService.class )
			);
		}
	}

	/**
	 * Adds an existing {@link EventHandlerBeanPostProcessor} bean to the bean factory.
	 *
	 * @param beanFactory to add the bean post processor to
	 */
	public static void registerEventHandlerBeanPostProcessor( ConfigurableListableBeanFactory beanFactory ) {
		try {
			EventHandlerBeanPostProcessor eventHandlerBeanPostProcessor
					= BeanFactoryUtils.beanOfTypeIncludingAncestors( beanFactory, EventHandlerBeanPostProcessor.class );
			eventHandlerBeanPostProcessor.registerExistingSingletons( beanFactory );

			beanFactory.addBeanPostProcessor( eventHandlerBeanPostProcessor );
		}
		catch ( NoSuchBeanDefinitionException ignore ) {
		}
	}
}
