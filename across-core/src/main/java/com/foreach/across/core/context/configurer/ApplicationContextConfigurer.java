package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.env.PropertySources;

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
	 * Returns a PropertySources instance with configured property sources to make available.
	 *
	 * @return PropertySources instance or null.
	 */
	PropertySources propertySources();
}
