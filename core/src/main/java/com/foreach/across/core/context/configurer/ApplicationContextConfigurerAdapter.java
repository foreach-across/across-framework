package com.foreach.across.core.context.configurer;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * Adapter class that implements the ApplicationContextConfigurer interface.
 * Provides default empty implementations for all beans.
 */
public abstract class ApplicationContextConfigurerAdapter implements ApplicationContextConfigurer
{
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
}
