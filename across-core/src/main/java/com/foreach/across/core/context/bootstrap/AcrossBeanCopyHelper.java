package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.context.ApplicationContextScanner;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.BeanDefinitionTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
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
	 * Manually add a singleton as copied.
	 *
	 * @param name  Name of the singleton bean.
	 * @param value Singleton instance.
	 */
	public void addSingleton( String name, Object value ) {
		singletonsCopied.put( name, value );
	}

	/**
	 * Copies all beans and definitions that match the filter, from the child to the parent context.
	 *
	 * @param child       Child context to copy from.
	 * @param parent      Parent context to copy to.
	 * @param filter      Filter that beans must match to be copied.
	 * @param transformer Transformer that will be applied to all matching beans before copying.
	 */
	public void copy( ConfigurableApplicationContext child,
	                  ConfigurableApplicationContext parent,
	                  BeanFilter filter,
	                  BeanDefinitionTransformer transformer ) {
		if ( filter == null ) {
			return;
		}

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( child, filter );
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( child, filter );

		if ( transformer != null ) {
			beans = transformer.transformSingletons( beans );
			definitions = transformer.transformBeanDefinitions( definitions );
		}

		ConfigurableListableBeanFactory parentFactory = parent.getBeanFactory();
		BeanDefinitionRegistry registry = null;

		if ( parentFactory instanceof BeanDefinitionRegistry ) {
			registry = (BeanDefinitionRegistry) parentFactory;
		}

		for ( Map.Entry<String, BeanDefinition> definition : definitions.entrySet() ) {
			if ( registry != null ) {
				BeanDefinition original = definition.getValue();

				if ( original.isSingleton() ) {
					GenericBeanDefinition simplified = new GenericBeanDefinition();
					simplified.setPrimary( original.isPrimary() );
					simplified.setAutowireCandidate( original.isAutowireCandidate() );
					simplified.setDescription( original.getDescription() );
					simplified.setRole( original.getRole() );
					simplified.setBeanClassName( original.getBeanClassName() );

					registry.registerBeanDefinition( definition.getKey(), simplified );
					definitionsCopied.put( definition.getKey(), simplified );
				}
				else {
					registry.registerBeanDefinition( definition.getKey(), original );
					definitionsCopied.put( definition.getKey(), original );
				}
			}
		}

		for ( Map.Entry<String, Object> singleton : beans.entrySet() ) {
			LOG.debug( "Exposing bean to parent context: {}", singleton );

			parentFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
			singletonsCopied.put( singleton.getKey(), singleton.getValue() );
		}
	}
}
