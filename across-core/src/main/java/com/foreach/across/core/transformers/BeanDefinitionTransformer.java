package com.foreach.across.core.transformers;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Map;

/**
 * Apply modification to a set of BeanDefinitions and singletons.
 * During the Across bootstrap the transformer is called before the exposed beans
 * are copied to the parent.  Can be used to rename beans, modify the BeanDefinition,
 * add beans etc.
 */
public interface BeanDefinitionTransformer
{
	/**
	 * Modify the collection of singletons.
	 *
	 * @param singletons Original map of singletons.
	 * @return Modified map of singletons.
	 */
	@Deprecated
	Map<String, Object> transformSingletons( Map<String, Object> singletons );

	/**
	 * Modify the collection of BeanDefinitions.
	 *
	 * @param beanDefinitions Original map of bean definitions.
	 * @return Modified map of bean definitions.
	 */
	Map<String, BeanDefinition> transformBeanDefinitions( Map<String, BeanDefinition> beanDefinitions );
}
