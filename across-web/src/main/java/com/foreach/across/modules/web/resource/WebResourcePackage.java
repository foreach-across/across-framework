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

package com.foreach.across.modules.web.resource;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a collection of web resources or related rules that should be applied to a registry.
 * A {@code WebResourcePackage} is usually identified by name in a particular package manager.
 *
 * @see WebResourceRule
 * @see WebResourcePackageManager
 */
public interface WebResourcePackage
{
	/**
	 * Install the resources contained in the package in the registry.
	 *
	 * @param registry Registry to which to add the resources.
	 */
	void install( WebResourceRegistry registry );

	/**
	 * Uninstall the package from the given registry.
	 *
	 * @param registry Registry where the package resource should be removed.
	 * @deprecated since 3.2.0 as the ability to uninstall is rarely needed and requires knowledge of the exact previous state of a registry
	 */
	@Deprecated
	default void uninstall( WebResourceRegistry registry ) {
	}

	/**
	 * Create a new package that executes the specified rules upon installation.
	 *
	 * @param rules to execute when installing the package in a registry
	 * @return package
	 */
	static WebResourcePackage of( WebResourceRule... rules ) {
		return of( Arrays.asList( rules ) );
	}

	/**
	 * Create a new package that executes the specified rules upon installation.
	 *
	 * @param rules to execute when installing the package in a registry
	 * @return package
	 */
	static WebResourcePackage of( @NonNull Collection<WebResourceRule> rules ) {
		return registry -> rules.forEach( rule -> rule.applyTo( registry ) );
	}

	/**
	 * Create a new package that combines two others: it first installs
	 * the {@code original} and then the {@code extension}.
	 *
	 * @param original  package to install
	 * @param extension package to install after the original
	 * @return package combine both
	 */
	static WebResourcePackage combine( @NonNull WebResourcePackage original, @NonNull WebResourcePackage extension ) {
		return registry -> {
			original.install( registry );
			extension.install( registry );
		};
	}
}
