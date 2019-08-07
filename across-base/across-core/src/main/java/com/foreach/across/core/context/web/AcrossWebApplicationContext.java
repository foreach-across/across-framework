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

package com.foreach.across.core.context.web;

import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.SharedMetadataReaderFactory;
import com.foreach.across.core.context.annotation.ModuleConfigurationBeanNameGenerator;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.support.ApplicationContextIdNameGenerator;
import com.foreach.across.core.context.support.MessageSourceBuilder;
import com.foreach.across.core.events.AcrossContextApplicationEventMulticaster;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * WebApplicationContext that allows a set of preregistered singletons to be passed in.
 * Differs from {@link com.foreach.across.core.context.AcrossApplicationContext} in when beans and definitions are
 * loaded, this context requires {@link #refresh()} to be called to load new bean definitions whereas
 * {@link com.foreach.across.core.context.AcrossApplicationContext} adds beans immediately.
 */
public class AcrossWebApplicationContext extends AnnotationConfigWebApplicationContext implements AcrossConfigurableApplicationContext
{
	private Collection<ProvidedBeansMap> providedBeansMaps = new LinkedHashSet<ProvidedBeansMap>();
	private final Map<String[], TypeFilter[]> packagesToScan = new HashMap<>();

	private Integer moduleIndex;

	public AcrossWebApplicationContext() {
		setId( ApplicationContextIdNameGenerator.forContext( this ) );
		setBeanNameGenerator( new ModuleConfigurationBeanNameGenerator() );
	}

	@Override
	public void setModuleIndex( Integer moduleIndex ) {
		this.moduleIndex = moduleIndex;
		if ( hasBeanFactory() ) {
			( (AcrossListableBeanFactory) getBeanFactory() ).setModuleIndex( moduleIndex );
		}
	}

	@Override
	protected DefaultListableBeanFactory createBeanFactory() {
		AcrossListableBeanFactory beanFactory = new AcrossListableBeanFactory( getInternalParentBeanFactory() );
		beanFactory.setModuleIndex( moduleIndex );
		return beanFactory;
	}

	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardAcrossServletEnvironment();
	}

	/**
	 * Adds a collection of provided beans to application context.
	 *
	 * @param beans One or more ProvidedBeansMaps to add.
	 */
	public void provide( ProvidedBeansMap... beans ) {
		for ( ProvidedBeansMap map : beans ) {
			if ( map != null ) {
				providedBeansMaps.add( map );
			}
		}
	}

	public void scan( String[] basePackages, TypeFilter[] excludedTypes ) {
		packagesToScan.put( basePackages, excludedTypes );
	}

	@Override
	protected void loadBeanDefinitions( DefaultListableBeanFactory beanFactory ) {
		for ( ProvidedBeansMap providedBeans : providedBeansMaps ) {
			for ( Map.Entry<String, BeanDefinition> definition : providedBeans.getBeanDefinitions().entrySet() ) {
				beanFactory.registerBeanDefinition( definition.getKey(), definition.getValue() );
			}
			for ( Map.Entry<String, Object> singleton : providedBeans.getSingletons().entrySet() ) {
				beanFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
			}
		}

		AnnotationConfigUtils.registerAnnotationConfigProcessors( beanFactory );
		SharedMetadataReaderFactory.registerAnnotationProcessors( beanFactory );

		super.loadBeanDefinitions( beanFactory );

		packagesToScan.forEach(
				( packages, filters ) -> {
					ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner( beanFactory );
					scanner.setBeanNameGenerator( getBeanNameGenerator() );
					scanner.setScopeMetadataResolver( getScopeMetadataResolver() );
					scanner.setResourcePattern( "**/*.class" );

					for ( TypeFilter filter : filters ) {
						scanner.addExcludeFilter( filter );
					}

					scanner.scan( packages );
				}
		);
	}

	@Override
	protected void initMessageSource() {
		new MessageSourceBuilder( getBeanFactory() ).build( getInternalParentMessageSource() );

		super.initMessageSource();
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
		// if parent context is also an AcrossApplicationContext - use its multicaster and re-register it
		final ApplicationContext parent = getParent();
		if ( parent instanceof AcrossConfigurableApplicationContext && !containsLocalBean( APPLICATION_EVENT_MULTICASTER_BEAN_NAME ) ) {
			final ApplicationEventMulticaster multicaster = parent.getBean( APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class );
			if ( multicaster instanceof AcrossContextApplicationEventMulticaster ) {
				getBeanFactory().registerSingleton(
						APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
						( (AcrossContextApplicationEventMulticaster) multicaster ).createModuleMulticaster( moduleIndex, getBeanFactory() )
				);
			}
		}

		super.initApplicationEventMulticaster();
	}
}
