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
 * Applies to all classes that are in one of the given packages.
 */
public class PackageBeanFilter extends AbstractClassBasedBeanFilter<String>
{
	public PackageBeanFilter( String... allowedPackages ) {
		super( allowedPackages );
	}

	public String[] getAllowedPackages() {
		return getAllowedItems();
	}

	public void setAllowedPackages( String[] allowedPackages ) {
		setAllowedItems( allowedPackages );
	}

	@Override
	protected boolean matches( Class beanClass, String expectedPackage ) {
		return matches( beanClass.getName(), expectedPackage );
	}

	@Override
	protected boolean matches( String beanClassName, String expectedPackage ) {
		return StringUtils.startsWith( beanClassName, expectedPackage );
	}
}
