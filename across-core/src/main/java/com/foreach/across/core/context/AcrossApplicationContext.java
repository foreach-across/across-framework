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
import com.foreach.across.core.events.AcrossContextApplicationEventMulticaster;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Map;

/**
 * ApplicationContext that allows a set of preregistered singletons to be passed in.
 */
public class AcrossApplicationContext extends AnnotationConfigApplicationContext implements AcrossConfigurableApplicationContext
{
	private boolean installerMode = false;
	private Integer moduleIndex;

	public AcrossApplicationContext() {
		this( new AcrossListableBeanFactory() );
	}

	private BeanNameGenerator beanNameGenerator;
	private ScopeMetadataResolver scopeMetadataResolver;

	protected AcrossApplicationContext( AcrossListableBeanFactory beanFactory ) {
		super( beanFactory );
		setBeanNameGenerator( new ModuleConfigurationBeanNameGenerator() );
	}

	@Override
	public void setModuleIndex( Integer moduleIndex ) {
		this.moduleIndex = moduleIndex;
		( (AcrossListableBeanFactory) getBeanFactory() ).setModuleIndex( moduleIndex );
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

	@Override
	public void setBeanNameGenerator( BeanNameGenerator beanNameGenerator ) {
		super.setBeanNameGenerator( beanNameGenerator );
		this.beanNameGenerator = beanNameGenerator;
	}

	@Override
	public void setScopeMetadataResolver( ScopeMetadataResolver scopeMetadataResolver ) {
		super.setScopeMetadataResolver( scopeMetadataResolver );
		this.scopeMetadataResolver = scopeMetadataResolver;
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
	protected void prepareBeanFactory( ConfigurableListableBeanFactory beanFactory ) {
		super.prepareBeanFactory( beanFactory );

		SharedMetadataReaderFactory.registerAnnotationProcessors( (BeanDefinitionRegistry) beanFactory );
	}

	@Override
	protected void publishEvent( Object event, ResolvableType eventType ) {
		super.publishEvent( event, eventType );
	}

	@Override
	protected void registerBeanPostProcessors( ConfigurableListableBeanFactory beanFactory ) {
		super.registerBeanPostProcessors( beanFactory );

		// Set the conversion service on the environment as well
		ConfigurableEnvironment environment = getEnvironment();

		if ( beanFactory.containsBean( CONVERSION_SERVICE_BEAN_NAME )
				&& beanFactory.isTypeMatch( CONVERSION_SERVICE_BEAN_NAME, ConfigurableConversionService.class ) ) {
			environment.setConversionService(
					beanFactory.getBean( CONVERSION_SERVICE_BEAN_NAME, ConfigurableConversionService.class )
			);
		}
	}

	@Override
	protected void initApplicationEventMulticaster() {
		if ( !installerMode ) {
			// if parent context is also an AcrossApplicationContext - use its multicaster and re-register it
			final ApplicationContext parent = getParent();
			if ( parent instanceof AcrossApplicationContext && !containsLocalBean( APPLICATION_EVENT_MULTICASTER_BEAN_NAME ) ) {
				final ApplicationEventMulticaster multicaster = parent.getBean( APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class );
				if ( multicaster instanceof AcrossContextApplicationEventMulticaster ) {
					getBeanFactory().registerSingleton(
							APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
							( (AcrossContextApplicationEventMulticaster) multicaster ).createModuleMulticaster( moduleIndex, getBeanFactory() )
					);
				}
			}
		}

		super.initApplicationEventMulticaster();
	}

	public void scan( String[] basePackages, TypeFilter[] excludedTypes ) {
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner( this, true, getEnvironment(), this );
		scanner.setBeanNameGenerator( beanNameGenerator );
		scanner.setScopeMetadataResolver( scopeMetadataResolver );
		scanner.setResourcePattern( "**/*.class" );

		for ( TypeFilter filter : excludedTypes ) {
			scanner.addExcludeFilter( filter );
		}

		scanner.scan( basePackages );
	}
}
