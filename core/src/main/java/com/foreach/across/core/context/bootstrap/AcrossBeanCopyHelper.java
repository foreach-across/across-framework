package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.context.ApplicationContextScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class AcrossBeanCopyHelper
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossBeanCopyHelper.class );

	private Map<String, Object> singletonsCopied = new HashMap<String, Object>();
	private Map<String, BeanDefinition> definitionsCopied = new HashMap<String, BeanDefinition>();

	public Map<String, Object> getSingletonsCopied() {
		return singletonsCopied;
	}

	public Map<String, BeanDefinition> getDefinitionsCopied() {
		return definitionsCopied;
	}

	/**
	 * Copies all beans and definitions that match the filter, from the child to the parent context.
	 *
	 * @param child  Child context to copy from.
	 * @param parent Parent context to copy to.
	 * @param filter Filter that beans must match to be copied.
	 */
	public void copy( ConfigurableApplicationContext child, ConfigurableApplicationContext parent, BeanFilter filter ) {
		if ( filter == null ) {
			return;
		}

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( child, filter );
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( child, filter );

		ConfigurableListableBeanFactory parentFactory = parent.getBeanFactory();
		BeanDefinitionRegistry registry = null;

		if ( parentFactory instanceof BeanDefinitionRegistry ) {
			registry = (BeanDefinitionRegistry) parentFactory;
		}

		for ( Map.Entry<String, Object> singleton : beans.entrySet() ) {
			LOG.debug( "Exposing bean to parent context: {}", singleton );

			parentFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
			singletonsCopied.put( singleton.getKey(), singleton.getValue() );
		}

		for ( Map.Entry<String, BeanDefinition> definition : definitions.entrySet() ) {
			if ( !definition.getValue().isSingleton() || !beans.containsKey( definition.getKey() ) ) {
				if ( registry != null ) {
					registry.registerBeanDefinition( definition.getKey(), definition.getValue() );
					definitionsCopied.put( definition.getKey(), definition.getValue() );
				}
			}
		}
	}
}
