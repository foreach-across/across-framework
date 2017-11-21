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
import lombok.NonNull;

import java.util.Map;

/**
 * Transformer that allows defining a new name for specific bean names.
 * Beans with an undefined name (without other name specified) can optionally be removed from the set.
 */
public class BeanRenameTransformer extends AbstractBeanRenameTransformer
{
	private final Map<String, String> renameMap;
	private final boolean removeUndefined;

	public BeanRenameTransformer( @NonNull Map<String, String> renameMap, boolean removeUndefined ) {
		this.renameMap = renameMap;
		this.removeUndefined = removeUndefined;
	}

	@Override
	protected String rename( String beanName, ExposedBeanDefinition definition ) {
		String name = renameMap.get( beanName );

		return name != null ? name : ( removeUndefined ? null : beanName );
	}
}
