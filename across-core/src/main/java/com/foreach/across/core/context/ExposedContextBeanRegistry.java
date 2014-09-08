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

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of all beans for an AcrossContext that are exposed to the parent ApplicationContext.
 * Because these can be transformed separately, clones of the original ExposedBeanDefinitions are used.
 */
public class ExposedContextBeanRegistry extends AbstractExposedBeanRegistry
{
	private final ConfigurableListableBeanFactory beanFactory;

	public ExposedContextBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry,
	                                   ConfigurableListableBeanFactory beanFactory,
	                                   ExposedBeanDefinitionTransformer transformer ) {
		super( contextBeanRegistry, null, transformer );

		this.beanFactory = beanFactory;
	}

	public void addAll( Map<String, ExposedBeanDefinition> exposedBeanDefinitions ) {
		Map<String, ExposedBeanDefinition> copies = new HashMap<>();

		for ( ExposedBeanDefinition original : exposedBeanDefinitions.values() ) {
			if ( transformer != null ) {
				ExposedBeanDefinition copy = new ExposedBeanDefinition( original );
				copies.put( copy.getFullyQualifiedBeanName(), copy );
			}
			else {
				copies.put( original.getFullyQualifiedBeanName(), original );
			}
		}

		if ( transformer != null ) {
			transformer.transformBeanDefinitions( copies );
		}

		exposedDefinitions.putAll( copies );
	}

	public void add( String... beans ) {
		Map<String, Object> singletons = new HashMap<>();
		Map<String, BeanDefinition> definitions = new HashMap<>();

		for ( String beanName : beans ) {
			if ( beanFactory.containsSingleton( beanName ) ) {
				singletons.put( beanName, beanFactory.getSingleton( beanName ) );
			}
			if ( beanFactory.containsBeanDefinition( beanName ) ) {
				definitions.put( beanName, beanFactory.getBeanDefinition( beanName ) );
			}
		}

		addBeans( definitions, singletons );
	}

	@Override
	protected void copyBeanDefinitions( ConfigurableListableBeanFactory beanFactory,
	                                    BeanDefinitionRegistry beanDefinitionRegistry ) {
		if ( !exposedDefinitions.isEmpty() ) {
			// Make sure the registry is present in the parent context
			beanFactory.registerSingleton( contextBeanRegistry.getFactoryName(), contextBeanRegistry );
		}

		super.copyBeanDefinitions( beanFactory, beanDefinitionRegistry );
	}
}
