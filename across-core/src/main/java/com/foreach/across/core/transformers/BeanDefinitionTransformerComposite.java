package com.foreach.across.core.transformers;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Map;

/**
 * Will apply all configured BeanDefinitionTransformers in the order registered.
 * The result of the current transformer will be used as the input for the next one, etc.
 */
public class BeanDefinitionTransformerComposite implements BeanDefinitionTransformer
{
	private BeanDefinitionTransformer[] transformers;

	public BeanDefinitionTransformerComposite( BeanDefinitionTransformer... transformers ) {
		this.transformers = transformers;
	}

	/**
	 * Modify the collection of singletons.
	 *
	 * @param singletons Original map of singletons.
	 * @return Modified map of singletons.
	 */
	public Map<String, Object> transformSingletons( Map<String, Object> singletons ) {
		Map<String, Object> modified = singletons;

		for ( BeanDefinitionTransformer transformer : transformers ) {
			modified = transformer.transformSingletons( modified );
		}

		return modified;
	}

	/**
	 * Modify the collection of BeanDefinitions.
	 *
	 * @param beanDefinitions Original map of bean definitions.
	 * @return Modified map of bean definitions.
	 */
	public Map<String, BeanDefinition> transformBeanDefinitions( Map<String, BeanDefinition> beanDefinitions ) {
		Map<String, BeanDefinition> modified = beanDefinitions;

		for ( BeanDefinitionTransformer transformer : transformers ) {
			modified = transformer.transformBeanDefinitions( modified );
		}

		return modified;
	}
}
