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

package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.env.PropertySources;

import java.util.Arrays;
import java.util.Objects;

/**
 * Adapter class that implements the ApplicationContextConfigurer interface.
 * Provides default empty implementations for all beans.
 */
public abstract class ApplicationContextConfigurerAdapter implements ApplicationContextConfigurer
{
	/**
	 * Returns a map of beans to register directly in the ApplicationContext.
	 * Provided beans will be registered first, before any of the annotated classes
	 * or defined packages are loaded.
	 *
	 * @return Map of bean name and value.
	 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
	 */
	public ProvidedBeansMap providedBeans() {
		return null;
	}

	/**
	 * Returns a set of annotated classes to register as components in the ApplicationContext.
	 * These can be annotated with @Configuration.
	 *
	 * @return Array of annotated classes.
	 */
	public Class[] annotatedClasses() {
		return new Class[0];
	}

	/**
	 * Return a set of packages that should be scanned for additional components.
	 *
	 * @return Array of package names.
	 */
	public String[] componentScanPackages() {
		return new String[0];
	}

	/**
	 * Returns a set of BeanFactoryPostProcessor instances to apply to the ApplicationContext.
	 *
	 * @return Array of post processor instances.
	 */
	public BeanFactoryPostProcessor[] postProcessors() {
		return new BeanFactoryPostProcessor[0];
	}

	/**
	 * Returns a PropertySources instance with configured property sources to make available.
	 *
	 * @return PropertySources instance or null.
	 */
	public PropertySources propertySources() {
		return null;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ApplicationContextConfigurerAdapter ) ) {
			return false;
		}

		ApplicationContextConfigurerAdapter that = (ApplicationContextConfigurerAdapter) o;

		if ( !Arrays.equals( annotatedClasses(), that.annotatedClasses() ) ) {
			return false;
		}
		if ( !Arrays.equals( componentScanPackages(), that.componentScanPackages() ) ) {
			return false;
		}
		if ( !Arrays.equals( postProcessors(), that.postProcessors() ) ) {
			return false;
		}
		if ( !Objects.equals( providedBeans(), that.providedBeans() ) ) {
			return false;
		}
		if ( !Objects.equals( propertySources(), that.propertySources() ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		Object providedBeans = providedBeans();
		String[] componentScanPackages = componentScanPackages();
		Class<?>[] annotatedClasses = annotatedClasses();
		BeanFactoryPostProcessor[] postProcessors = postProcessors();
		PropertySources propertySources = propertySources();

		int result = annotatedClasses != null ? Arrays.hashCode( annotatedClasses ) : 0;
		result = 31 * result + ( componentScanPackages != null ? Arrays.hashCode( componentScanPackages ) : 0 );
		result = 31 * result + ( postProcessors != null ? Arrays.hashCode( postProcessors ) : 0 );
		result = 31 * result + ( providedBeans != null ? providedBeans.hashCode() : 0 );
		result = 31 * result + ( propertySources != null ? propertySources.hashCode() : 0 );
		return result;
	}
}
