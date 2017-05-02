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

package com.foreach.across.modules.web.context;

import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.annotation.ModuleConfigurationBeanNameGenerator;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.support.MessageSourceBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Collection;
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

	public AcrossWebApplicationContext() {
		setBeanNameGenerator( new ModuleConfigurationBeanNameGenerator() );
	}

	@Override
	protected DefaultListableBeanFactory createBeanFactory() {
		return new AcrossListableBeanFactory( getInternalParentBeanFactory() );
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

		super.loadBeanDefinitions( beanFactory );
	}

	@Override
	protected void initMessageSource() {
		new MessageSourceBuilder( getBeanFactory() ).build( getInternalParentMessageSource() );

		super.initMessageSource();
	}

	@Override
	protected void registerBeanPostProcessors( ConfigurableListableBeanFactory beanFactory ) {
		super.registerBeanPostProcessors( beanFactory );

		AcrossApplicationContext.registerEventHandlerBeanPostProcessor( beanFactory );

		// Set the conversion service on the environment as well
		ConfigurableEnvironment environment = getEnvironment();

		if ( beanFactory.containsBean( CONVERSION_SERVICE_BEAN_NAME )
				&& beanFactory.isTypeMatch( CONVERSION_SERVICE_BEAN_NAME, ConfigurableConversionService.class ) ) {
			environment.setConversionService(
					beanFactory.getBean( CONVERSION_SERVICE_BEAN_NAME, ConfigurableConversionService.class )
			);
		}
	}
}
