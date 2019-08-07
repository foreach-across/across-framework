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

import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.regex.Pattern;

/**
 * Simple implementation for specifying packages an ApplicationContext should scan.
 * Packages can be specified as {@code String} or by providing a {@code Class} in that package.
 */
public class ComponentScanConfigurer extends ApplicationContextConfigurerAdapter
{
	private final String[] packages;
	private final TypeFilter[] excludedTypeFilters;

	public ComponentScanConfigurer( String... packages ) {
		this( packages, new TypeFilter[0] );
	}

	public ComponentScanConfigurer( Class<?>... packageClasses ) {
		this( packageClasses, new TypeFilter[0] );
	}

	public ComponentScanConfigurer( String[] packages, TypeFilter[] excludedTypeFilters ) {
		this.packages = packages.clone();
		this.excludedTypeFilters = excludedTypeFilters.clone();
	}

	public ComponentScanConfigurer( Class<?>[] packageClasses, TypeFilter[] excludedTypeFilters ) {
		this.packages = new String[packageClasses.length];

		for ( int i = 0; i < packageClasses.length; i++ ) {
			packages[i] = packageClasses[i].getPackage().getName();
		}

		this.excludedTypeFilters = excludedTypeFilters.clone();
	}

	/**
	 * Return a set of packages that should be scanned for additional components.
	 *
	 * @return Array of package names.
	 */
	@Override
	public String[] componentScanPackages() {
		return packages.clone();
	}

	@Override
	public TypeFilter[] excludedTypeFilters() {
		return excludedTypeFilters;
	}

	/**
	 * Create a component scan configurer for an AcrossModule.
	 * The entire package the module belongs to will be included, with the exception
	 * of the *extensions* or *installers* package.
	 *
	 * @param moduleClass representing the module descriptor
	 * @return configurer
	 */
	public static ComponentScanConfigurer forAcrossModule( Class<?> moduleClass ) {
		return forAcrossModulePackage( moduleClass.getPackage().getName() );
	}

	/**
	 * Create a component scan configurer for an AcrossModule.
	 * The entire package the module belongs to will be included, with the exception
	 * of the *extensions* or *installers* package.
	 *
	 * @param packageName representing the module descriptor
	 * @return configurer
	 */
	public static ComponentScanConfigurer forAcrossModulePackage( String packageName ) {
		return new ComponentScanConfigurer( new String[] { packageName },
		                                    new TypeFilter[] {
				                                    new RegexPatternTypeFilter( Pattern.compile( packageName + ".(extensions|installers).+" ) )
		                                    } );
	}
}
