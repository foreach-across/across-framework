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

/**
 * Simple implementation for specifying packages an ApplicationContext should scan.
 * Packages can be specified as {@code String} or by providing a {@code Class} in that package.
 */
public class ComponentScanConfigurer extends ApplicationContextConfigurerAdapter
{
	private String[] packages;

	public ComponentScanConfigurer( String... packages ) {
		this.packages = packages;
	}

	public ComponentScanConfigurer( Class<?>... packageClasses ) {
		this.packages = new String[packageClasses.length];

		for ( int i = 0; i < packageClasses.length; i++ ) {
			packages[i] = packageClasses[i].getPackage().getName();
		}
	}

	/**
	 * Return a set of packages that should be scanned for additional components.
	 *
	 * @return Array of package names.
	 */
	@Override
	public String[] componentScanPackages() {
		return packages;
	}
}
