/*
 * Copyright 2019 the original author or authors
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

package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.env.PropertySources;
import org.springframework.core.type.filter.TypeFilter;

public interface ApplicationContextConfigurer
{
	/**
	 * Returns a map of beans to register directly in the ApplicationContext.
	 * Provided beans will be registered first, before any of the annotated classes
	 * or defined packages are loaded.
	 *
	 * @return Map of bean name and value.
	 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
	 */
	ProvidedBeansMap providedBeans();

	/**
	 * Returns a set of annotated classes to register as components in the ApplicationContext.
	 * These can be annotated with @Configuration.
	 *
	 * @return Array of annotated classes.
	 */
	Class[] annotatedClasses();

	/**
	 * Return a set of packages that should be scanned for additional components.
	 *
	 * @return Array of package names.
	 */
	String[] componentScanPackages();

	/**
	 * Returns a set of BeanFactoryPostProcessor instances to apply to the ApplicationContext.
	 *
	 * @return Array of post processor instances.
	 */
	BeanFactoryPostProcessor[] postProcessors();

	/**
	 * @return set of excludedTypeFilters
	 */
	default TypeFilter[] excludedTypeFilters() {
		return new TypeFilter[0];
	}

	/**
	 * Returns a PropertySources instance with configured property sources to make available.
	 *
	 * @return PropertySources instance or null.
	 */
	PropertySources propertySources();

	/**
	 * Checks if the configurer contains any actual components (beans) that would get created.
	 *
	 * @return true if any components need to be created
	 */
	default boolean hasComponents() {
		ProvidedBeansMap providedBeans = providedBeans();
		return !ArrayUtils.isEmpty( componentScanPackages() )
				|| !ArrayUtils.isEmpty( annotatedClasses() )
				|| !ArrayUtils.isEmpty( postProcessors() )
				|| ( providedBeans != null && !providedBeans.isEmpty() );
	}

	/**
	 * Checks if the configurer should be considered optional. Optional configurers should
	 * not force a module to be loaded, they often provide infrastructure components that are
	 * only relevant if there are at least some other beans.
	 *
	 * @return true if this configuration should be considered optional
	 */
	default boolean isOptional() {
		return false;
	}

	/**
	 * Makes a configurer optional.
	 *
	 * @param configurer to make optional
	 * @return new configurer
	 */
	static ApplicationContextConfigurer optional( @NonNull ApplicationContextConfigurer configurer ) {
		return new ApplicationContextConfigurer()
		{
			@Override
			public ProvidedBeansMap providedBeans() {
				return configurer.providedBeans();
			}

			@Override
			public Class[] annotatedClasses() {
				return configurer.annotatedClasses();
			}

			@Override
			public String[] componentScanPackages() {
				return configurer.componentScanPackages();
			}

			@Override
			public BeanFactoryPostProcessor[] postProcessors() {
				return configurer.postProcessors();
			}

			@Override
			public PropertySources propertySources() {
				return configurer.propertySources();
			}

			@Override
			public TypeFilter[] excludedTypeFilters() {
				return configurer.excludedTypeFilters();
			}

			@Override
			public boolean isOptional() {
				return true;
			}

			@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
			@Override
			public boolean equals( Object obj ) {
				return configurer.equals( obj ) && ( (ApplicationContextConfigurer) obj ).isOptional() == isOptional();
			}

			@Override
			public int hashCode() {
				return 31 * configurer.hashCode() + ( configurer.isOptional() ? 0 : 1 );
			}
		};
	}
}
