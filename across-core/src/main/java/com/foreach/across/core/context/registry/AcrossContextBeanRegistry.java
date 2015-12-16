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

package com.foreach.across.core.context.registry;

import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Map;

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
	 * Check if a bean is registered under the given name in the AcrossContext.
	 *
	 * @param beanName Name of the bean definition.
	 * @return True if bean registered.
	 */
	boolean containsBean( String beanName );

	/**
	 * Check if a bean is registered under the given name, directly in a module.
	 *
	 * @param moduleName Name of the module.
	 * @param beanName   Name of the bean definition.
	 * @return True if bean registered.
	 */
	boolean moduleContainsLocalBean( String moduleName, String beanName );

	/**
	 * Get a bean registered under the given name in the AcrossContext.
	 *
	 * @param beanName Name of the bean definition.
	 * @return Instance.
	 */
	Object getBean( String beanName );

	/**
	 * Get a bean registered under the given name in the AcrossContext.
	 *
	 * @param beanName     Name of the bean definition.
	 * @param requiredType of the bean
	 * @return Instance.
	 */
	<T> T getBean( String beanName, Class<T> requiredType );

	/**
	 * Determine the type for the bean registered under the given name in the AcrossContext.
	 *
	 * @param beanName Name of the bean definition.
	 * @return Type if it could be resolved.
	 */
	Class<?> getBeanType( String beanName );

	/**
	 * Get a bean registered under the given name from the ApplicationContext of the given module.
	 *
	 * @param moduleName Unique name of the module.
	 * @param beanName   Name of the bean definition.
	 * @return Instance.
	 */
	<T> T getBeanFromModule( String moduleName, String beanName );

	/**
	 * Determine the type for the bean registered under the given name in the module ApplicationContext.
	 *
	 * @param moduleName Unique name of the module.
	 * @param beanName   Name of the bean definition.
	 * @return Type if it could be resolved.
	 */
	Class<?> getBeanTypeFromModule( String moduleName, String beanName );

	/**
	 * Searches the across context for a bean of the given type.
	 *
	 * @param requiredType Type the bean should match.
	 * @param <T>          Type of the matching bean.
	 * @return Bean found.  Exception is thrown if none is found.
	 */
	<T> T getBeanOfType( Class<T> requiredType );

	/**
	 * Searches the ApplicationContext of the specific module for a bean of the given type.
	 *
	 * @param moduleName   Unique name of the module.
	 * @param requiredType Type the bean should match.
	 * @param <T>          Type of the matching bean.
	 * @return Bean found.  Exception is thrown if none is found.
	 */
	<T> T getBeanOfTypeFromModule( String moduleName, Class<T> requiredType );

	/**
	 * <p>Collect all beans of a given type that are visible inside this bean registry.
	 * This includes beans from ancestors, but does not include module internal beans.</p>
	 * <p>All beans will be sorted according to the Order, module index and OrderInModule values.</p>
	 *
	 * @param beanClass Type of bean to look for.
	 * @param <T>       Specific bean type.
	 * @return List of bean instances.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> List<T> getBeansOfType( Class<T> beanClass );

	/**
	 * <p>Collect all beans of a given type that are visible inside this bean registry.
	 * This includes beans from ancestors, but does not include module internal beans.</p>
	 * <p>All beans will be sorted according to the Order, module index and OrderInModule values.</p>
	 * <p>Beans will be returned as a Map where the key is the bean name.</p>
	 *
	 * @param beanClass Type of bean to look for.
	 * @param <T>       Specific bean type.
	 * @return Map of bean instances with their bean name and/or module information.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> Map<String, T> getBeansOfTypeAsMap( Class<T> beanClass );

	/**
	 * <p>Collect all beans of a given type that are visible inside this bean registry.
	 * Depending on the second parameter, module internal beans will be included.</p>
	 * <p>All beans will be sorted according to the Order, module index and OrderInModule values.</p>
	 *
	 * @param beanClass Type of bean to look for.
	 * @param <T>       Specific bean type.
	 * @return List of bean instances.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> List<T> getBeansOfType( Class<T> beanClass, boolean includeModuleInternals );

	/**
	 * <p>Collect all beans of a given type that are visible inside this bean registry.
	 * Depending on the second parameter, module internal beans will be included.</p>
	 * <p>All beans will be sorted according to the Order, module index and OrderInModule values.</p>
	 * <p>
	 * Beans will be returned as a Map where the key is the bean name if module internals are not included
	 * or the bean is from a parent context.  If module internals are included, the bean name will be prefixed
	 * with module name.
	 * </p>
	 *
	 * @param beanClass Type of bean to look for.
	 * @param <T>       Specific bean type.
	 * @return Map of bean instances with their bean name and/or module information.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> Map<String, T> getBeansOfTypeAsMap( Class<T> beanClass, boolean includeModuleInternals );

	/**
	 * <p>Collect all beans of a given type that are visible inside this bean registry.
	 * Depending on the second parameter, module internal beans will be included.</p>
	 * <p>All beans will be sorted according to the Order, module index and OrderInModule values.</p>
	 * <p>This method allows looking for beans having specific generic parameters.</p>
	 *
	 * @param resolvableType Type of bean to look for.
	 * @param <T>            Specific bean type.
	 * @return List of bean instances.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> List<T> getBeansOfType( ResolvableType resolvableType, boolean includeModuleInternals );

	/**
	 * <p>Collect all beans of a given type that are visible inside this bean registry.
	 * Depending on the second parameter, module internal beans will be included.</p>
	 * <p>All beans will be sorted according to the Order, module index and OrderInModule values.</p>
	 * <p>This method allows looking for beans having specific generic parameters.</p>
	 * <p>
	 * Beans will be returned as a Map where the key is the bean name if module internals are not included
	 * or the bean is from a parent context.  If module internals are included, the bean name will be prefixed
	 * with module name.
	 * </p>
	 *
	 * @param resolvableType Type of bean to look for.
	 * @param <T>            Specific bean type.
	 * @return Map of bean instances with their bean name and/or module information.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> Map<String, T> getBeansOfTypeAsMap( ResolvableType resolvableType, boolean includeModuleInternals );
}
