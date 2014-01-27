package com.foreach.across.core.context.configurer;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

public interface ApplicationContextConfigurer
{
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
}
