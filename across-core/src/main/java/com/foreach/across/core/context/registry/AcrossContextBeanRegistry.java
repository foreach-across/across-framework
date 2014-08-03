package com.foreach.across.core.context.registry;

/**
 * Provides access to all BeanFactories present in an AcrossContext.
 */
public interface AcrossContextBeanRegistry
{
	String BEAN = "across.contextBeanRegistry";

	/**
	 * Get a bean registered under the given name from the ApplicationContext of the given module.
	 *
	 * @param moduleName Unique name of the module.
	 * @param beanName Name of the bean definition.
	 * @return Instance.
	 */
	Object getBeanFromModule( String moduleName, String beanName );
}
