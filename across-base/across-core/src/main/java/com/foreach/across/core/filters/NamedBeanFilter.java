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

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Filters beans on a set of allowed bean names.
 */
public class NamedBeanFilter implements BeanFilter
{
	private final String[] allowedNames;

	public NamedBeanFilter( String... allowedNames ) {
		this.allowedNames = allowedNames;
	}

	@Override
	public boolean apply( ConfigurableListableBeanFactory beanFactory,
	                      String beanName,
	                      Object bean,
	                      BeanDefinition definition ) {
		return ArrayUtils.contains( allowedNames, beanName );
	}
}
