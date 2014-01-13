package com.foreach.across.core.util;

import com.foreach.across.test.filters.AnnotationBeanFilter;
import com.foreach.across.test.filters.BeanFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.util.*;

public class ApplicationContextScanner
{
	/**
	 * Will find all beans in the ApplicationContext that have been created with the given annotation.
	 * This includes both beans having the annotation directly, as beans created through a Configuration
	 * class @Bean method where the annotation was present on the method definition.
	 * <p/>
	 * Note: only singleton or actually created beans will be returned.
	 *
	 * @param annotation Required annotation.
	 * @return List of beans, never null.
	 */
	public static Collection<Object> findBeansWithAnnotation( ApplicationContext context,
	                                                          Class<? extends Annotation> annotation ) {

		return findSingletonsMatching( context, new AnnotationBeanFilter( annotation ) ).values();

		  /*
		ConfigurableListableBeanFactory beanFactory =
				(ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory();

		Set<Object> matchingBeans = new HashSet<Object>();

		// Add all singletons having the Refreshable annotation
		for ( Object singleton : context.getBeansWithAnnotation( annotation ).values() ) {
			matchingBeans.add( singleton );
		}

		// Get all bean definitions declared by method having the annotation
		for ( String name : beanFactory.getBeanDefinitionNames() ) {
			BeanDefinition def = beanFactory.getBeanDefinition( name );

			if ( def.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) def.getSource();

				if ( metadata.isAnnotated( annotation.getName() ) && def.isSingleton() ) {
					matchingBeans.add( beanFactory.getBean( name ) );
				}
			}
		}

		return matchingBeans;*/
	}

	public static Map<String, Object> findSingletonsMatching( ApplicationContext context, BeanFilter filter ) {
		HashMap<String, Object> beanMap = new HashMap<String, Object>();
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) context;

		ConfigurableListableBeanFactory beanFactory = ctx.getBeanFactory();

		List<String> definitions = Arrays.asList( beanFactory.getBeanDefinitionNames() );

		for ( String singletonName : beanFactory.getSingletonNames() ) {
			BeanDefinition definition =
					definitions.contains( singletonName ) ? beanFactory.getBeanDefinition( singletonName ) : null;
			Object bean = ctx.getBeanFactory().getSingleton( singletonName );

			if ( filter.apply( bean, definition ) ) {
				beanMap.put( singletonName, bean );
			}
		}

		return beanMap;
	}

	public static Map<String, BeanDefinition> findBeanDefinitionsMatching( ApplicationContext context,
	                                                                       BeanFilter filter ) {
		HashMap<String, BeanDefinition> definitionMap = new HashMap<String, BeanDefinition>();
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) context;

		ConfigurableListableBeanFactory beanFactory = ctx.getBeanFactory();

		for ( String defName : ctx.getBeanDefinitionNames() ) {
			BeanDefinition def = ctx.getBeanFactory().getBeanDefinition( defName );
			Object bean = beanFactory.getSingleton( defName );

			if ( filter.apply( bean, def ) ) {
				definitionMap.put( defName, def );
			}

		}

		return definitionMap;
	}
}
