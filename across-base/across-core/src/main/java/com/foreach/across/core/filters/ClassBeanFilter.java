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

import org.apache.commons.lang3.StringUtils;

/**
 * Filters on any number of specific classes.
 */
public class ClassBeanFilter extends AbstractClassBasedBeanFilter<Class>
{
	public ClassBeanFilter( Class... allowedClasses ) {
		super( allowedClasses );
	}

	public Class[] getAllowedClasses() {
		return getAllowedItems();
	}

	public void setAllowedClasses( Class[] allowedClasses ) {
		setAllowedItems( allowedClasses );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean matches( Class beanClass, Class expected ) {
		return expected.isAssignableFrom( beanClass );
	}

	@Override
	protected boolean matches( String beanClassName, Class expected ) {
		return StringUtils.equalsIgnoreCase( beanClassName, expected.getName() );
	}
}
