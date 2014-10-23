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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExposedBeanRegistry
{
	@SuppressWarnings("all")
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	protected final String moduleName;
	protected final AcrossContextBeanRegistry contextBeanRegistry;
	protected final ExposedBeanDefinitionTransformer transformer;

	protected final Map<String, ExposedBeanDefinition> exposedDefinitions = new HashMap<>();

	protected AbstractExposedBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry,
	                                       String moduleName,
	                                       ExposedBeanDefinitionTransformer transformer ) {
		this.moduleName = moduleName;
		this.contextBeanRegistry = contextBeanRegistry;
		this.transformer = transformer;
	}

	public Map<String, ExposedBeanDefinition> getExposedDefinitions() {
		return Collections.unmodifiableMap( exposedDefinitions );
	}

	protected void addBeans( Map<String, BeanDefinition> definitions, Map<String, Object> beans ) {
		Map<String, ExposedBeanDefinition> candidates = new HashMap<>();

		for ( Map.Entry<String, BeanDefinition> definition : definitions.entrySet() ) {
			if ( !isScopedTarget( definition.getKey() ) ) {
				BeanDefinition original = definition.getValue();
				ExposedBeanDefinition exposed = new ExposedBeanDefinition(
						contextBeanRegistry,
						moduleName,
						definition.getKey(),
						original,
						contextBeanRegistry.getBeanTypeFromModule( moduleName, definition.getKey() )
				);

				candidates.put( definition.getKey(), exposed );
			}
		}

		for ( Map.Entry<String, Object> singleton : beans.entrySet() ) {
			if ( !candidates.containsKey( singleton.getKey() )
					&& singleton.getValue() != null
					&& !isScopedTarget( singleton.getKey() ) ) {
				ExposedBeanDefinition exposed = new ExposedBeanDefinition(
						contextBeanRegistry,
						moduleName,
						singleton.getKey(),
						contextBeanRegistry.getBeanTypeFromModule( moduleName, singleton.getKey() )
				);

				candidates.put( singleton.getKey(), exposed );
			}
		}

		if ( transformer != null ) {
			transformer.transformBeanDefinitions( candidates );
		}

		exposedDefinitions.putAll( candidates );
	}

	private boolean isScopedTarget( String name ) {
		return StringUtils.startsWith( name, "scopedTarget." );
	}

	/**
	 * Copies the BeanDefinitions to the BeanFactory provided (if possible).
	 */
	public void copyTo( ConfigurableListableBeanFactory beanFactory ) {
		if ( beanFactory instanceof BeanDefinitionRegistry ) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			copyBeanDefinitions( beanFactory, registry );
		}
		else {
			LOG.warn(
					"Unable to copy exposed bean definitions to bean factory {}, " +
							"it is not a BeanDefinitionRegistry",
					beanFactory );
		}
	}

	protected void copyBeanDefinitions( ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry ) {
		for ( Map.Entry<String, ExposedBeanDefinition> definition : exposedDefinitions.entrySet() ) {
			LOG.debug( "Exposing bean {}: {}", definition.getKey(), definition.getValue().getBeanClassName() );

			ExposedBeanDefinition beanDefinition = definition.getValue();

			String beanName = beanDefinition.getPreferredBeanName();

			if ( beanFactory.containsBean( beanName ) ) {
				LOG.trace(
						"BeanDefinitionRegistry already contains a bean with name {}, using fully qualified name for exposing",
						beanName );
				beanName = beanDefinition.getFullyQualifiedBeanName();
			}

			registry.registerBeanDefinition( beanName, beanDefinition );

			for ( String alias : beanDefinition.getAliases() ) {
				registry.registerAlias( definition.getKey(), alias );
			}
		}
	}
}
