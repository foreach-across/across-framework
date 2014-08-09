package com.foreach.across.core.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;

import java.util.Map;

/**
 * Will apply all configured BeanDefinitionTransformers in the order registered.
 * The result of the current transformer will be used as the input for the next one, etc.
 */
public class BeanDefinitionTransformerComposite implements ExposedBeanDefinitionTransformer
{
	private ExposedBeanDefinitionTransformer[] transformers;

	public BeanDefinitionTransformerComposite( ExposedBeanDefinitionTransformer... transformers ) {
		this.transformers = transformers;
	}

	public void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions ) {
		for ( ExposedBeanDefinitionTransformer transformer : transformers ) {
			transformer.transformBeanDefinitions( beanDefinitions );
		}
	}
}
