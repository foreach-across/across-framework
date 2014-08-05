package com.foreach.across.core.context;

import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.BeanDefinitionTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
	private static final Logger LOG = LoggerFactory.getLogger( ExposedBeanRegistry.class );

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
			ExposedBeanDefinition exposed = new ExposedBeanDefinition(
					moduleInfo.getContextInfo().getId(),
					moduleInfo.getName(),
					definition.getKey(),
					original,
					determineBeanClass( original, beans.get( definition.getKey() ) )
			);

			candidates.put( beanDefinitionName( moduleInfo, definition.getKey() ), exposed );
		}

		for ( Map.Entry<String, Object> singleton : beans.entrySet() ) {
			if ( !candidates.containsKey( singleton.getKey() ) && singleton.getValue() != null ) {
				ExposedBeanDefinition exposed = new ExposedBeanDefinition(
						moduleInfo.getContextInfo().getId(),
						moduleInfo.getName(),
						singleton.getKey(),
						determineBeanClass( null, singleton.getValue() )
				);

				candidates.put( beanDefinitionName( moduleInfo, singleton.getKey() ), exposed );
			}
		}

		exposedDefinitions.putAll( transformer != null ?
				                           transformer.transformBeanDefinitions( candidates ) : candidates );
	}

	private String beanDefinitionName( ConfigurableAcrossModuleInfo moduleInfo, String originalName ) {
		return moduleInfo.getContextInfo().getId() + "." + moduleInfo.getName() + "@" + originalName;
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

	public Map<String, BeanDefinition> getExposedDefinitions() {
		return Collections.unmodifiableMap( exposedDefinitions );
	}

	/**
	 * Copies the BeanDefinitions to the ApplicationContext provided (if possible).
	 *
	 * @param context ApplicationContext that should have a BeanDefinitionRegistry.
	 */
	public void copyTo( ConfigurableApplicationContext context ) {
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

		if ( beanFactory instanceof BeanDefinitionRegistry ) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;

			for ( Map.Entry<String, BeanDefinition> definition : exposedDefinitions.entrySet() ) {
				LOG.debug( "Exposing bean {}: {}", definition.getKey(), definition.getValue().getBeanClassName() );

				/*
				beanFactory.registerResolvableDependency( ((ExposedBeanDefinition) definition.getValue()).getBeanClass(),
				                                          new ObjectFactory()
				                                          {
					                                          @Override
					                                          public Object getObject() throws BeansException {
						                                          return null;
					                                          }
				                                          });
				                                          */
				ExposedBeanDefinition beanDefinition = (ExposedBeanDefinition) definition.getValue();

				String beanName = beanDefinition.getPreferredBeanName();
				boolean registerAlias = false;

				if ( context.containsBean( beanName ) ) {
					registerAlias = true;
					beanName = beanDefinition.getFullyQualifierBeanName();
				}

				registry.registerBeanDefinition( beanName, beanDefinition );

				if ( registerAlias ) {
					registry.registerAlias( beanName, beanDefinition.getPreferredBeanName() );
				}

				for ( String alias : beanDefinition.getAliases() ) {
						registry.registerAlias( definition.getKey(), alias );
				}
			}
		}
		else {
			LOG.warn( "Unable to copy exposed bean definitions to application context {}", context );
		}
	}
}
