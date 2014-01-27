package com.foreach.across.core.context.configurer;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * Simple implementation for adding BeanFactoryPostProcessors to an ApplicationContext.
 */
public class PostProcessorConfigurer extends ApplicationContextConfigurerAdapter
{
	private BeanFactoryPostProcessor[] postProcessors;

	public PostProcessorConfigurer( BeanFactoryPostProcessor... postProcessors ) {
		this.postProcessors = postProcessors;
	}

	/**
	 * Returns a set of BeanFactoryPostProcessor instances to apply to the ApplicationContext.
	 *
	 * @return Array of post processor instances.
	 */
	@Override
	public BeanFactoryPostProcessor[] postProcessors() {
		return postProcessors;
	}
}
