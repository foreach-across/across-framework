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

package com.foreach.across.core.context.configurer;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * Simple implementation for adding BeanFactoryPostProcessors to an ApplicationContext.
 * Note that post processor instances passed this way will be attached to the application context
 * and bean factory that created them, and not the one of the AcrossContext or AcrossModule that
 * is being bootstrapped.
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
		return postProcessors.clone();
	}
}
