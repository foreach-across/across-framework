package com.foreach.across.core.context;

import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
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

		if ( filter == null ) {
			return;
		}

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( child, filter );
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( child, filter );

		Map<String, ExposedBeanDefinition> candidates = new HashMap<>();

		for ( Map.Entry<String, BeanDefinition> definition : definitions.entrySet() ) {
			if ( !isFactoryBean( definition.getValue() ) ) {
				BeanDefinition original = definition.getValue();
				ExposedBeanDefinition exposed = new ExposedBeanDefinition(
						contextBeanRegistry,
						moduleInfo.getName(),
						definition.getKey(),
						original,
						determineBeanClass( original, beans.get( definition.getKey() ) )
				);

				candidates.put( definition.getKey(), exposed );
			}
		}

		for ( Map.Entry<String, Object> singleton : beans.entrySet() ) {
			if ( !candidates.containsKey( singleton.getKey() )
					&& singleton.getValue() != null
					&& !isFactoryBean( singleton.getValue() ) ) {
				ExposedBeanDefinition exposed = new ExposedBeanDefinition(
						contextBeanRegistry,
						moduleInfo.getName(),
						singleton.getKey(),
						determineBeanClass( null, singleton.getValue() )
				);

				candidates.put( singleton.getKey(), exposed );
			}
		}

		if ( transformer != null ) {
			transformer.transformBeanDefinitions( candidates );
		}

		exposedDefinitions.putAll( candidates );
	}

	private boolean isFactoryBean( BeanDefinition beanDefinition ) {
		// FactoryBeans are not exposed if they have a target bean definition
		return false;
	}

	private boolean isFactoryBean( Object singleton ) {
		return false;// singleton instanceof FactoryBean;
	}

	private Class<?> determineBeanClass( BeanDefinition beanDefinition, Object singleton ) {
		if ( beanDefinition instanceof AbstractBeanDefinition ) {
			AbstractBeanDefinition originalAbstract = (AbstractBeanDefinition) beanDefinition;

			if ( !originalAbstract.hasBeanClass() ) {
				try {
					originalAbstract.resolveBeanClass( Thread.currentThread().getContextClassLoader() );
				}
				catch ( Exception e ) {
					throw new RuntimeException( e );
				}
			}

			if ( originalAbstract.hasBeanClass() ) {
				return originalAbstract.getBeanClass();
			}
		}

		if ( singleton != null ) {
			return singleton.getClass();
		}

		return null;
	}
}
