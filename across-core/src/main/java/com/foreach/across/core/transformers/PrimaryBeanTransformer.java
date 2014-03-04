package com.foreach.across.core.transformers;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Collection;
import java.util.Map;

/**
 * <p>Will register the bean definitions as primary beans.  Optionally a list of the bean names
 * that should be set primary can be defined.</p>
 * <p>Note that this will in fact modify the original BeanDefinition.</p>
 */
public class PrimaryBeanTransformer implements BeanDefinitionTransformer
{
	private Collection<String> beanNames;

	public PrimaryBeanTransformer() {
	}

	public PrimaryBeanTransformer( Collection<String> beanNames ) {
		this.beanNames = beanNames;
	}

	/**
	 * Modify the collection of singletons.
	 *
	 * @param singletons Original map of singletons.
	 * @return Modified map of singletons.
	 */
	public Map<String, Object> transformSingletons( Map<String, Object> singletons ) {
		return singletons;
	}

	/**
	 * Modify the collection of BeanDefinitions.
	 *
	 * @param beanDefinitions Original map of bean definitions.
	 * @return Modified map of bean definitions.
	 */
	public Map<String, BeanDefinition> transformBeanDefinitions( Map<String, BeanDefinition> beanDefinitions ) {
		for ( Map.Entry<String, BeanDefinition> definition : beanDefinitions.entrySet() ) {
			makePrimary( definition.getKey(), definition.getValue() );
		}

		return beanDefinitions;
	}

	private void makePrimary( String beanName, BeanDefinition definition ) {
		if ( !definition.isPrimary() ) {
			if ( beanNames == null || beanNames.contains( beanName ) ) {
				definition.setPrimary( true );
			}
		}
	}
}
