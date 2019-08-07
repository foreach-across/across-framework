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
package com.foreach.across.core.context.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author Arne Vandamme
 */
public class BeanFactoryAnnotationUtils
{
	/**
	 * Retrieve the annotation attributes for a particular annotation type of a bean.  The annotation can be
	 * registered either on the bean type directly, or the factory method or factory type for the bean.
	 *
	 * @param beanFactory    that contains the bean definition
	 * @param beanName       name of the bean
	 * @param annotationType type of annotation to look for
	 * @return annotation attributes if annotation was found
	 */
	public static Optional<AnnotationAttributes> findAnnotationOnBean( BeanFactory beanFactory,
	                                                                   String beanName,
	                                                                   Class<? extends Annotation> annotationType ) {
		if ( beanFactory instanceof ConfigurableListableBeanFactory ) {
			ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory) beanFactory;

			if ( bf.containsBean( beanName ) ) {
				try {
					BeanDefinition bd = bf.getMergedBeanDefinition( beanName );
					if ( bd instanceof RootBeanDefinition ) {
						Method factoryMethod = ( (RootBeanDefinition) bd ).getResolvedFactoryMethod();
						if ( factoryMethod != null ) {
							Annotation targetAnnotation
									= AnnotationUtils.getAnnotation( factoryMethod, annotationType );

							if ( targetAnnotation != null ) {
								return optional( targetAnnotation );
							}

							Class<?> factoryClass = factoryMethod.getDeclaringClass();
							Annotation annotation = AnnotationUtils.getAnnotation( factoryClass, annotationType );

							if ( annotation != null ) {
								return optional( annotation );
							}
						}

					}

					Class<?> beanClass = bf.getType( beanName );
					Annotation annotation = AnnotationUtils.getAnnotation( beanClass, annotationType );

					if ( annotation != null ) {
						return optional( annotation );
					}
				}
				catch ( NoSuchBeanDefinitionException ignore ) {
				}
			}
		}
		else {
			throw new IllegalArgumentException( "BeanFactory does not implement ConfigurableListableBeanFactory" );
		}

		return Optional.empty();
	}

	private static Optional<AnnotationAttributes> optional( Annotation annotation ) {
		return Optional.of(
				new AnnotationAttributes( AnnotationUtils.getAnnotationAttributes( annotation ) )
		);
	}
}
