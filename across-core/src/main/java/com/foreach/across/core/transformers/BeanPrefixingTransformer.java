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
import org.apache.commons.lang3.StringUtils;

/**
 * <p>Will add a prefix to all bean names, and will camelCase if required (default).</p>
 * <p><strong>Eg.</strong> with prefix test: sessionFactory would become testSessionFactory</p>
 * <p>By default only prefixes the primary bean name, if you want to include alias renames as well,
 * you should use {@link #BeanPrefixingTransformer(String, boolean, boolean)}.</p>
 */
public class BeanPrefixingTransformer extends AbstractBeanRenameTransformer
{
	private final String prefix;
	private final boolean camelCase;

	public BeanPrefixingTransformer( String prefix ) {
		this( prefix, true, true );
	}

	public BeanPrefixingTransformer( String prefix, boolean camelCase, boolean includeAliases ) {
		super( includeAliases );

		this.prefix = prefix;
		this.camelCase = camelCase;
	}

	@Override
	protected String rename( String beanName, ExposedBeanDefinition definition ) {
		return prefix + ( camelCase ? StringUtils.capitalize( beanName ) : beanName );
	}
}
