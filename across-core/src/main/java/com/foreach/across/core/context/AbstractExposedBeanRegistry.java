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
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractExposedBeanRegistry
{
	@SuppressWarnings("all")
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	protected final String moduleName;
	protected final Integer moduleIndex;
	protected final AcrossContextBeanRegistry contextBeanRegistry;
	protected final ExposedBeanDefinitionTransformer transformer;

	protected final Map<String, ExposedBeanDefinition> exposedDefinitions = new LinkedHashMap<>();

	protected AbstractExposedBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry,
	                                       String moduleName,
	                                       Integer moduleIndex,
	                                       ExposedBeanDefinitionTransformer transformer ) {
		this.moduleName = moduleName;
		this.moduleIndex = moduleIndex;
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
						moduleIndex,
						definition.getKey(),
						original,
						contextBeanRegistry.getBeanTypeFromModule( moduleName, definition.getKey() ),
						getAliases( definition.getKey() ) );

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
						moduleIndex,
						singleton.getKey(),
						contextBeanRegistry.getBeanTypeFromModule( moduleName, singleton.getKey() ),
						getAliases( singleton.getKey() ) );

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
		copyTo( beanFactory, true );
	}

	public void copyTo( ConfigurableListableBeanFactory beanFactory, boolean ignoreExistingBeanName ) {
		if ( beanFactory instanceof BeanDefinitionRegistry ) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			copyBeanDefinitions( beanFactory, registry, ignoreExistingBeanName );
		}
		else {
			LOG.warn(
					"Unable to copy exposed bean definitions to bean factory {}, " +
							"it is not a BeanDefinitionRegistry",
					beanFactory );
		}
	}

	public boolean isEmpty() {
		return exposedDefinitions.isEmpty();
	}

	protected void copyBeanDefinitions( ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry, boolean ignoreExistingBeanName ) {
		LOG.trace( "Exposing beans to bean factory {}", beanFactory );
		for ( Map.Entry<String, ExposedBeanDefinition> definition : exposedDefinitions.entrySet() ) {
			LOG.trace( "Exposing bean {}: {}", definition.getKey(), definition.getValue().getBeanClassName() );

			ExposedBeanDefinition beanDefinition = definition.getValue();

			String beanName = beanDefinition.getPreferredBeanName();

			if ( beanFactory.containsLocalBean( beanName ) ) {
				if ( ignoreExistingBeanName ) {
					LOG.trace(
							"BeanDefinitionRegistry already contains a bean with name {}, using fully qualified name for exposing",
							beanName );
					beanName = beanDefinition.getFullyQualifiedBeanName();
				}
				else {
					continue;
				}
			}

			registry.registerBeanDefinition( beanName, beanDefinition );

			for ( String alias : beanDefinition.getAliases() ) {
				AcrossContextUtils.registerBeanDefinitionAlias( registry, beanName, alias );
			}
		}
	}

	protected abstract String[] getAliases( String beanName );
}
