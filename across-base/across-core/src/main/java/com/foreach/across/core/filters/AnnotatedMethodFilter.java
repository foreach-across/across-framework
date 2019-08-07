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

package com.foreach.across.core.filters;

import com.foreach.across.core.util.ClassLoadingUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Filter that searches for beans having a method with the specific annotation.
 */
public class AnnotatedMethodFilter implements BeanFilter
{
	private final Class<? extends Annotation> annotationClass;

	public AnnotatedMethodFilter( Class<? extends Annotation> annotationClass ) {
		this.annotationClass = annotationClass;
	}

	/**
	 * Checks if a bean or its corresponding BeanDefinition match the filter.
	 *
	 * @param beanFactory BeanFactory that owns the bean and definition.
	 * @param beanName    Name of the bean to check
	 * @param bean        Bean instance to check (can be null).
	 * @param definition  BeanDefinition corresponding to this bean (can be null).   @return True if the bean and bean definition match.
	 */
	@SuppressWarnings({ "findbugs:DE_MIGHT_IGNORE", "squid:S00108" })
	public boolean apply( ConfigurableListableBeanFactory beanFactory,
	                      String beanName,
	                      Object bean,
	                      BeanDefinition definition ) {
		if ( bean != null ) {
			Class beanClass = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( bean ) );

			for ( Method method : ReflectionUtils.getUniqueDeclaredMethods( beanClass ) ) {
				if ( AnnotationUtils.getAnnotation( method, annotationClass ) != null ) {
					return true;
				}
			}
		}

		if ( definition != null ) {

			if ( bean == null && definition.getBeanClassName() != null ) {
				try {
					Class beanClass = ClassLoadingUtils.loadClass( definition.getBeanClassName() );

					for ( Method method : ReflectionUtils.getUniqueDeclaredMethods( beanClass ) ) {
						if ( AnnotationUtils.getAnnotation( method, annotationClass ) != null ) {
							return true;
						}
					}
				}
				catch ( ClassNotFoundException ignore ) { /* Ignore any exceptions */ }
			}

			// Still possible that we are dealing with a ScopedProxyFactoryBean, in which case we need to check the target
			if ( definition instanceof RootBeanDefinition ) {
				BeanDefinitionHolder targetHolder = ( (RootBeanDefinition) definition ).getDecoratedDefinition();

				if ( targetHolder != null ) {
					Object targetBean = beanFactory.getSingleton( targetHolder.getBeanName() );
					return apply( beanFactory, targetHolder.getBeanName(), targetBean,
					              targetHolder.getBeanDefinition() );
				}
			}
		}

		return false;
	}
}
