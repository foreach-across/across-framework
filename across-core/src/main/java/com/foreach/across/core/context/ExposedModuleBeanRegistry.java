package com.foreach.across.core.context;

import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * Builds a set of BeanDefinitions that should be exposed to other contexts.
 *
 * @author Arne Vandamme
 */
public class ExposedModuleBeanRegistry extends AbstractExposedBeanRegistry
{
	public ExposedModuleBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry,
	                                  ConfigurableAcrossModuleInfo moduleInfo,
	                                  ConfigurableApplicationContext child,
	                                  BeanFilter filter,
	                                  ExposedBeanDefinitionTransformer transformer ) {
		super( contextBeanRegistry, moduleInfo.getName(), transformer );

		if ( filter == null ) {
			return;
		}

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( child, filter );
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( child, filter );

		addBeans( definitions, beans );
	}
}
