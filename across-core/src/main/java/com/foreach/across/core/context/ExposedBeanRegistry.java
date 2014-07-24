package com.foreach.across.core.context;

import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.BeanDefinitionTransformer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds a set of BeanDefinitions that should be exposed to other contexts.
 *
 * @author Arne Vandamme
 */
public class ExposedBeanRegistry
{
	private final Map<String, BeanDefinition> exposedDefinitions = new HashMap<>();

	public ExposedBeanRegistry( ConfigurableAcrossModuleInfo moduleInfo,
	                            ConfigurableApplicationContext child,
	                            BeanFilter filter,
	                            BeanDefinitionTransformer transformer ) {

		if ( filter == null ) {
			return;
		}

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( child, filter );
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( child, filter );

		Map<String, BeanDefinition> candidates = new HashMap<>();

		for ( Map.Entry<String, BeanDefinition> definition : definitions.entrySet() ) {
			BeanDefinition original = definition.getValue();
			ExposedBeanDefinition exposed = new ExposedBeanDefinition( moduleInfo, definition.getKey(), original );

			candidates.put( definition.getKey(), exposed );
		}

		for ( Map.Entry<String, Object> singleton : beans.entrySet() ) {
			if ( !candidates.containsKey( singleton.getKey() ) && singleton.getValue() != null ) {
				ExposedBeanDefinition exposed = new ExposedBeanDefinition( moduleInfo, singleton.getKey() );

				candidates.put( singleton.getKey(), exposed );
			}
		}

		exposedDefinitions.putAll( transformer.transformBeanDefinitions( candidates ) );
	}

	public Map<String, BeanDefinition> getExposedDefinitions() {
		return Collections.unmodifiableMap( exposedDefinitions );
	}
}
