package com.foreach.across.core.context.registry;

/**
 * Provides access to all BeanFactories present in an AcrossContext.
 */
public interface AcrossContextBeanRegistry
{
	String BEAN = "across.contextBeanRegistry";

	/**
	 * @return The unique id of the AcrossContext this registry represents.
	 */
	String getContextId();

	/**
	 * The factory name is the unique bean name under which the registry can be found.
	 *
	 * @return The unique name of this AcrossContextBeanRegistry.
	 */
	String getFactoryName();

	/**
	 * Get a bean registered under the given name from the ApplicationContext of the given module.
	 *
	 * @param moduleName Unique name of the module.
	 * @param beanName   Name of the bean definition.
	 * @return Instance.
	 */
	Object getBeanFromModule( String moduleName, String beanName );
}
