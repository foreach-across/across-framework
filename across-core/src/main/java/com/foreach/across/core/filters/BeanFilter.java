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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Helper used to filter the beans that should be exposed to the parent context.
 */
public interface BeanFilter
{
	/**
	 * Checks if a bean or its corresponding BeanDefinition match the filter.
	 *
	 * @param beanFactory BeanFactory that owns the bean and definition.
	 * @param beanName    Name of the bean in the beanFactory.
	 * @param bean        Bean instance to check (can be null).
	 * @param definition  BeanDefinition corresponding to this bean (can be null).   @return True if the bean and bean definition match.
	 */
	boolean apply( ConfigurableListableBeanFactory beanFactory,
	               String beanName,
	               Object bean,
	               BeanDefinition definition );

	/**
	 * @return a filter that does not match on any bean
	 */
	static BeanFilter empty() {
		return ( beanFactory, beanName, bean, definition ) -> false;
	}

	/**
	 * Creates an {@link AnnotationBeanFilter} for the given classes.
	 * This method is lenient and both {@code null} objects and non-{@link java.lang.annotation.Annotation}
	 * instances can be passed.  Only non-{@code null} classes extending {@link java.lang.annotation.Annotation}
	 * will be taken into account.
	 *
	 * @param classes annotation classes
	 * @return filter matching on the annotations
	 */
	@SuppressWarnings("unchecked")
	static BeanFilter annotations( Class<?>... classes ) {
		return new AnnotationBeanFilter(
				true, true,
				(Class<Annotation>[])
						Stream.of( classes )
						      .filter( c -> c != null && Annotation.class.isAssignableFrom( c ) )
						      .toArray( Class[]::new )
		);
	}

	/**
	 * Creates a {@link ClassBeanFilter} for the given classes.
	 * This method is lenient: any {@code null} parameter value and
	 * classes extending {@link java.lang.annotation.Annotation} will be ignored.
	 *
	 * @param classes the beans should match
	 * @return filter matching those classes
	 */
	static BeanFilter instances( Class<?>... classes ) {
		return new ClassBeanFilter(
				Stream.of( classes )
				      .filter( c -> c != null && !Annotation.class.isAssignableFrom( c ) )
				      .toArray( Class[]::new )
		);
	}

	/**
	 * Creates a {@link NamedBeanFilter} for the given bean names.
	 * This method is lenient: {@code null} bean names will be ignored.
	 *
	 * @param beanNames that should apply
	 * @return filter matching those bean names
	 */
	static BeanFilter beanNames( String... beanNames ) {
		return new NamedBeanFilter(
				Stream.of( beanNames )
				      .filter( n -> n != null )
				      .toArray( String[]::new )
		);
	}

	/**
	 * Creates a {@link PackageBeanFilter} for the given package names.
	 * This method is lenient: {@code null} package names will be ignored.
	 *
	 * @param packageNames from which beans should apply
	 * @return filter matching the packages
	 */
	static BeanFilter packages( String... packageNames ) {
		return new PackageBeanFilter(
				Stream.of( packageNames )
				      .filter( n -> n != null )
				      .toArray( String[]::new )
		);
	}

	/**
	 * Creates a {@link PackageBeanFilter} for the given package names.
	 * Typesafe equivalent of {@link #packages(String...)}.  This method is also
	 * lenient: {@code null} parameters will be ignored.
	 *
	 * @param packageClasses from whose packages the beans should apply
	 * @return filter matching the packages
	 */
	static BeanFilter packages( Class<?>... packageClasses ) {
		return new PackageBeanFilter(
				Stream.of( packageClasses )
				      .filter( n -> n != null )
				      .map( c -> c.getPackage().getName() )
				      .toArray( String[]::new )
		);
	}

	/**
	 * Creates a {@link BeanFilterComposite} for a given array of filters.  This method is lenient, any {@code null}
	 * instances will simply be ignored.
	 *
	 * @param beanFilters to combine
	 * @return composite filter - never null
	 */
	static BeanFilter composite( BeanFilter... beanFilters ) {
		return new BeanFilterComposite(
				Stream.of( beanFilters )
				      .filter( b -> b != null )
				      .toArray( BeanFilter[]::new )
		);
	}

	/**
	 * Creates a {@link BeanFilterComposite} for the given class names: if a class is present on the classpath and
	 * is not an annotation, it will be checked for instance.  If it is an annotation it will check if the annotation
	 * is present either on the class or on the factory method that created it.
	 *
	 * @param classNames to add
	 * @return composite filter - never null
	 */
	static BeanFilter classNames( String... classNames ) {
		Class[] classesOrAnnotations = Stream.of( classNames )
		                                     .map( ClassLoadingUtils::resolveClass )
		                                     .filter( Objects::nonNull )
		                                     .toArray( Class[]::new );

		if ( classesOrAnnotations.length > 0 ) {
			return new BeanFilterComposite( instances( classesOrAnnotations ), annotations( classesOrAnnotations ) );
		}

		return empty();
	}
}
