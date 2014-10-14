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
