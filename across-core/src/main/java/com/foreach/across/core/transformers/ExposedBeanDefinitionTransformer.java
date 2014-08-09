package com.foreach.across.core.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;

import java.util.Map;

/**
 * Apply modification to a set of BeanDefinitions and singletons.
 * During the Across bootstrap the transformer is called before the exposed beans
 * are copied to the parent.  Can be used to rename beans, modify the BeanDefinition,
 * add beans etc.
 */
public interface ExposedBeanDefinitionTransformer
{
	/**
	 * Modify the collection of ExposedBeanDefinitions.
	 *
	 * @param beanDefinitions Map of exposed bean definitions.
	 */
	void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions );
}
