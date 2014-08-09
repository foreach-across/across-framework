package com.foreach.across.core.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExposedBeanRegistry
{
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	protected final Map<String, ExposedBeanDefinition> exposedDefinitions = new HashMap<>();

	public Map<String, ExposedBeanDefinition> getExposedDefinitions() {
		return Collections.unmodifiableMap( exposedDefinitions );
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
