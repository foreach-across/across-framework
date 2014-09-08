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

package com.foreach.across.core.filters;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Helper used to filter the beans that should be exposed to the parent context.
 */
public interface BeanFilter
{
	/**
	 * Checks if a bean or its corresponding BeanDefinition match the filter.
	 *
	 * @param beanFactory BeanFactory that owns the bean and definition.
	 * @param beanName    Name of the bean in the beanFactory.
	 * @param bean        Bean instance to check (can be null).
	 * @param definition  BeanDefinition corresponding to this bean (can be null).   @return True if the bean and bean definition match.
	 */
	boolean apply( ConfigurableListableBeanFactory beanFactory,
	               String beanName,
	               Object bean,
	               BeanDefinition definition );
}
