package com.foreach.across.core.transformers;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBeanRenameTransformer implements BeanDefinitionTransformer
{
	/**
	 * Modify the collection of singletons.
	 *
	 * @param singletons Original map of singletons.
	 * @return Modified map of singletons.
	 */
	public Map<String, Object> transformSingletons( Map<String, Object> singletons ) {
		Map<String, Object> modified = new HashMap<String, Object>();

		for ( Map.Entry<String, Object> singleton : singletons.entrySet() ) {
			String name = rename( singleton.getKey(), singleton.getValue() );

			if ( name != null ) {
				modified.put( name, singleton.getValue() );
			}
		}

		return modified;
	}

	/**
	 * Modify the collection of BeanDefinitions.
	 *
	 * @param beanDefinitions Original map of bean definitions.
	 * @return Modified map of bean definitions.
	 */
	public Map<String, BeanDefinition> transformBeanDefinitions( Map<String, BeanDefinition> beanDefinitions ) {
		Map<String, BeanDefinition> modified = new HashMap<String, BeanDefinition>();

		for ( Map.Entry<String, BeanDefinition> definition : beanDefinitions.entrySet() ) {
			String name = rename( definition.getKey(), definition.getValue() );

			if ( name != null ) {
				modified.put( name, definition.getValue() );
			}
		}

		return modified;
	}

	protected abstract String rename( String beanName, Object valueOrDefinition );
}
