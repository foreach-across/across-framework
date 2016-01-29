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

import com.foreach.across.core.annotations.Module;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ModuleBeanSelectorUtils
{

	/**
	 * Utility method that will find a bean of given type in the provided beanFactory.
	 * This method will try to find a bean annotated with the {@link Module} annotation with the provided moduleName as value.
	 * <p/>
	 * If no bean annotated with a {@link Module} annotation with moduleName as value was found,
	 * this method will default to a bean of the given type <em>without</em> a {@link Module} annotation
	 *
	 * @param type        The {@link Class} of the required type of the requested bean
	 * @param moduleName  The required value of the {@link Module} annotation for the requested bean
	 * @param beanFactory The beanFactory which will be used to find the requested bean
	 * @return Either an {@link Optional} filled with the requested bean
	 * Or an {@link Optional#empty()} in case no bean was found
	 */
	public static <T> Optional<T> selectBeanForModule( Class<T> type,
	                                                   String moduleName,
	                                                   ConfigurableListableBeanFactory beanFactory ) throws NoUniqueBeanDefinitionException {
		String[] definedBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors( beanFactory, type );

		Set<String> moduleBeanNames = new HashSet<>();
		Set<String> defaultBeanNames = new HashSet<>();

		for ( String beanName : definedBeans ) {
			String beanModule = determineModuleAnnotationValue( beanFactory, beanName );

			if ( StringUtils.isEmpty( beanModule ) ) {
				defaultBeanNames.add( beanName );
			}
			else if ( StringUtils.equals( moduleName, beanModule ) ) {
				moduleBeanNames.add( beanName );
			}
		}

		if ( !moduleBeanNames.isEmpty() ) {
			if ( moduleBeanNames.size() > 1 ) {
				throw new NoUniqueBeanDefinitionException( type, moduleBeanNames );
			}

			return Optional.of( beanFactory.getBean( moduleBeanNames.iterator().next(), type ) );
		}

		if ( !defaultBeanNames.isEmpty() ) {
			if ( defaultBeanNames.size() > 1 ) {
				throw new NoUniqueBeanDefinitionException( type, defaultBeanNames );
			}
			return Optional.of( beanFactory.getBean( defaultBeanNames.iterator().next(), type ) );
		}

		return Optional.empty();
	}

	private static String determineModuleAnnotationValue( ConfigurableListableBeanFactory beanFactory,
	                                                      String beanName ) {
		Optional<AnnotationAttributes> annotationAttributes = BeanFactoryAnnotationUtils.findAnnotationOnBean(
				beanFactory, beanName, Module.class );

		if ( annotationAttributes.isPresent() ) {
			return annotationAttributes.get().getString( "value" );
		}
		else {
			return null;
		}
	}
}
