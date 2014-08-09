package com.foreach.across.core.context;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
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
	private final AcrossContextBeanRegistry contextBeanRegistry;
	private final ExposedBeanDefinitionTransformer transformer;

	public ExposedContextBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry,
	                                   ExposedBeanDefinitionTransformer transformer ) {
		this.contextBeanRegistry = contextBeanRegistry;
		this.transformer = transformer;
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

	@Override
	protected void copyBeanDefinitions( ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry beanDefinitionRegistry ) {
		if ( !exposedDefinitions.isEmpty() ) {
			// Make sure the registry is present in the parent context
			beanFactory.registerSingleton( contextBeanRegistry.getFactoryName(), contextBeanRegistry );
		}

		super.copyBeanDefinitions( beanFactory, beanDefinitionRegistry );
	}
}
