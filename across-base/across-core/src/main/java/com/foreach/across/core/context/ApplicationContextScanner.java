/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.core.context;

import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.filters.BeanFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.*;

public final class ApplicationContextScanner
{
	private ApplicationContextScanner() {
	}

	/**
	 * <p>Will find all beans in the ApplicationContext that have been created with the given annotation.
	 * Will search recursively for the annotation, a match will occur if the bean implements an interface
	 * having the annotation or was created through a Configuration class @Bean method where the annotation
	 * was present on the method definition.</p>
	 * <p>Note: only singleton or actually created beans will be returned.</p>
	 *
	 * @param annotation Required annotation.
	 * @return List of beans, never null.
	 */
	public static Collection<Object> findBeansWithAnnotation( ApplicationContext context,
	                                                          Class<? extends Annotation> annotation ) {

		return findSingletonsMatching( context, new AnnotationBeanFilter( true, true, annotation ) ).values();
	}

	public static Map<String, Object> findSingletonsMatching( ApplicationContext context, BeanFilter filter ) {
		Assert.isInstanceOf( ConfigurableListableBeanFactory.class, context.getAutowireCapableBeanFactory() );

		return findSingletonsMatching( (ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory(),
		                               filter );
	}

	/**
	 * Does not consider exposed bean definitions.
	 */
	public static Map<String, Object> findSingletonsMatching( ConfigurableListableBeanFactory beanFactory,
	                                                          BeanFilter filter ) {
		Map<String, Object> beanMap = new HashMap<String, Object>();

		List<String> definitions = Arrays.asList( beanFactory.getBeanDefinitionNames() );

		for ( String singletonName : beanFactory.getSingletonNames() ) {
			BeanDefinition definition =
					definitions.contains( singletonName ) ? beanFactory.getBeanDefinition( singletonName ) : null;

			if ( !( definition instanceof ExposedBeanDefinition ) ) {
				Object bean = beanFactory.getSingleton( singletonName );

				if ( filter.apply( beanFactory, singletonName, bean, definition ) ) {
					beanMap.put( singletonName, bean );
				}
			}
		}

		return beanMap;
	}

	public static Map<String, BeanDefinition> findBeanDefinitionsMatching( ApplicationContext context,
	                                                                       BeanFilter filter ) {
		Assert.isInstanceOf( ConfigurableListableBeanFactory.class, context.getAutowireCapableBeanFactory() );

		return findBeanDefinitionsMatching( (ConfigurableListableBeanFactory) context.getAutowireCapableBeanFactory(),
		                                    filter );
	}

	/**
	 * Does not consider exposed bean definitions.
	 */
	public static Map<String, BeanDefinition> findBeanDefinitionsMatching( ConfigurableListableBeanFactory beanFactory,
	                                                                       BeanFilter filter ) {
		Map<String, BeanDefinition> definitionMap = new HashMap<String, BeanDefinition>();

		for ( String defName : beanFactory.getBeanDefinitionNames() ) {
			BeanDefinition def = beanFactory.getMergedBeanDefinition( defName );
			if ( !( def instanceof ExposedBeanDefinition ) ) {
				Object bean = beanFactory.getSingleton( defName );

				if ( filter.apply( beanFactory, defName, bean, def ) ) {
					definitionMap.put( defName, def );
				}
			}
		}

		return definitionMap;
	}
}
