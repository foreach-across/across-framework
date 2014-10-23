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

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * ApplicationContext that allows a set of preregistered singletons to be passed in.
 */
public class AcrossApplicationContext extends AnnotationConfigApplicationContext implements AcrossConfigurableApplicationContext
{
	private Collection<ProvidedBeansMap> providedBeansMaps = new LinkedHashSet<ProvidedBeansMap>();

	public AcrossApplicationContext() {
		super( new AcrossListableBeanFactory() );
	}

	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardAcrossEnvironment();
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

	/**
	 * Configure the factory's standard context characteristics,
	 * such as the context's ClassLoader and post-processors.
	 *
	 * @param beanFactory the BeanFactory to configure
	 */
	@Override
	protected void prepareBeanFactory( ConfigurableListableBeanFactory beanFactory ) {
		super.prepareBeanFactory( beanFactory );

		for ( ProvidedBeansMap providedBeans : providedBeansMaps ) {
			for ( Map.Entry<String, BeanDefinition> definition : providedBeans.getBeanDefinitions().entrySet() ) {
				registerBeanDefinition( definition.getKey(), definition.getValue() );
			}
			for ( Map.Entry<String, Object> singleton : providedBeans.getSingletons().entrySet() ) {
				beanFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
			}
		}
	}
}
