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
 * Apply modification to a set of BeanDefinitions and singletons.
 * During the Across bootstrap the transformer is called before the exposed beans
 * are copied to the parent.  Can be used to rename beans, modify the BeanDefinition,
 * add beans etc.
 */
public interface ExposedBeanDefinitionTransformer
{
	/**
	 * Simple transformer that removes all ExposedBeanDefinitions, ensuring that no beans will be exposed.
	 * Use this instance on the {@link com.foreach.across.core.AcrossContext} to disable exposing to the parent
	 * ApplicationContext.
	 */
	ExposedBeanDefinitionTransformer REMOVE_ALL = new ExposedBeanDefinitionTransformer()
	{
		@Override
		public void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions ) {
			beanDefinitions.clear();
		}
	};

	/**
	 * Modify the collection of ExposedBeanDefinitions.
	 *
	 * @param beanDefinitions Map of exposed bean definitions.
	 */
	void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions );
}
