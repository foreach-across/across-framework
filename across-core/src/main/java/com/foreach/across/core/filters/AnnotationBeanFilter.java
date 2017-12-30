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
import lombok.NonNull;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Beans or definitions with any of the given annotations will be copied.
 */
public class AnnotationBeanFilter implements BeanFilter
{
	private boolean matchIfBeanFactoryApplies = false;
	private boolean useRecursiveSearch = false;
	private Class<? extends Annotation>[] annotations;

	public AnnotationBeanFilter( @NonNull Class<? extends Annotation>... annotations ) {
		this.annotations = annotations.clone();
	}

	public AnnotationBeanFilter( boolean matchIfBeanFactoryApplies,
	                             boolean useRecursiveSearch,
	                             @NonNull Class<? extends Annotation>... annotations ) {
		this.matchIfBeanFactoryApplies = matchIfBeanFactoryApplies;
		this.useRecursiveSearch = useRecursiveSearch;
		this.annotations = annotations.clone();
	}

	public Class<? extends Annotation>[] getAnnotations() {
		return annotations.clone();
	}

	public void setAnnotations( @NonNull Class<? extends Annotation>[] annotations ) {
		this.annotations = annotations.clone();
	}

	public boolean isMatchIfBeanFactoryApplies() {
		return matchIfBeanFactoryApplies;
	}

	/**
	 * @param matchIfBeanFactoryApplies True if the bean should be returned if the bean factory matches.
	 */
	public void setMatchIfBeanFactoryApplies( boolean matchIfBeanFactoryApplies ) {
		this.matchIfBeanFactoryApplies = matchIfBeanFactoryApplies;
	}

	@SuppressWarnings({ "findbugs:DE_MIGHT_IGNORE", "squid:S00108" })
	public boolean apply( ConfigurableListableBeanFactory beanFactory,
	                      String beanName,
	                      Object bean,
	                      BeanDefinition definition ) {
		if ( bean != null ) {
			Class targetClass = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( bean ) );

			if ( useRecursiveSearch ) {
				if ( hasAnnotation( bean.getClass() ) ) {
					return true;
				}
			}

			if ( hasAnnotation( targetClass ) ) {
				return true;
			}

			if ( bean instanceof FactoryBean ) {
				// in case of a factory bean, check it as well
				targetClass = ( (FactoryBean) bean ).getObjectType();

				if ( hasAnnotation( targetClass ) ) {
					return true;
				}
			}
		}

		// Even though a bean might not match, perhaps the definition that created it does
		if ( definition != null ) {
			if ( definition.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) definition.getSource();

				// If method itself has the annotation it applies
				for ( Class<? extends Annotation> annotation : annotations ) {
					if ( metadata.isAnnotated( annotation.getName() ) ) {
						return true;
					}
				}

				try {
					Class factoryClass = ClassUtils.getUserClass(
							ClassLoadingUtils.loadClass( metadata.getDeclaringClassName() ) );

					if ( definition.getFactoryBeanName() != null ) {
						Object factory = beanFactory.getSingleton( definition.getFactoryBeanName() );

						if ( factory != null ) {
							factoryClass = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( factory ) );
						}
					}
					if ( isMatchIfBeanFactoryApplies() ) {
						// If the bean factory has the annotation, then it should apply as well
						for ( Class<? extends Annotation> annotation : annotations ) {
							if ( AnnotationUtils.getAnnotation( factoryClass, annotation ) != null ) {
								return true;
							}
						}
					}

					if ( bean == null ) {
						// If the target of the method has the annotation, it applies - in case of a bean
						// this has already been tested
						Method method = ReflectionUtils.findMethod( factoryClass, metadata.getMethodName() );

						if ( method != null ) {
							for ( Class<? extends Annotation> annotation : annotations ) {
								if ( AnnotationUtils.getAnnotation( method.getReturnType(), annotation ) != null ) {
									return true;
								}
							}
						}
					}
				}
				catch ( Exception ignore ) { /* Ignore any exceptions */ }
			}
			else if ( bean == null && definition.getBeanClassName() != null ) {
				try {
					Class beanClass = ClassLoadingUtils.loadClass( definition.getBeanClassName() );

					for ( Class<? extends Annotation> annotation : annotations ) {
						if ( AnnotationUtils.getAnnotation( beanClass, annotation ) != null ) {
							return true;
						}
					}
				}
				catch ( Exception ignore ) { /* Ignore any exceptions */ }
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

	private boolean hasAnnotation( Class beanClass ) {
		for ( Class<? extends Annotation> annotation : annotations ) {
			Annotation found;

			if ( useRecursiveSearch ) {
				found = AnnotationUtils.findAnnotation( beanClass, annotation );
			}
			else {
				found = AnnotationUtils.getAnnotation( beanClass, annotation );
			}

			if ( found != null ) {
				return true;
			}
		}

		return false;
	}
}
