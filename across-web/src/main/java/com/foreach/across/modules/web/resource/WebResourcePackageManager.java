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

import java.util.*;

/**
 * Allows registration of {@link WebResourcePackage} instances under a specific name.
 * When attached to a {@link WebResourceRegistry}, the caller can simply do {@link WebResourceRegistry#addPackage(String...)}
 * to install all resources bundled by the specific package.
 *
 * @see WebResourceRegistry
 * @see WebResourcePackage
 */
public class WebResourcePackageManager
{
	private final Map<String, WebResourcePackage> packages = new HashMap<>();

	public void register( @NonNull String name, @NonNull WebResourcePackage webResourcePackage ) {
		packages.put( name, webResourcePackage );
	}

	/**
	 * @return names of the registered packages
	 */
	public Set<String> getPackageNames() {
		return packages.keySet();
	}

	/**
	 * Remove a package.
	 *
	 * @param name of the package to remove
	 * @return package that has been removed
	 */
	public Optional<WebResourcePackage> unregister( @NonNull String name ) {
		return Optional.ofNullable( packages.remove( name ) );
	}

	/**
	 * Get the package with the given name.
	 *
	 * @param name of the package
	 * @return resource package
	 */
	public WebResourcePackage getPackage( @NonNull String name ) {
		return packages.get( name );
	}

	/**
	 * Extend the package registered under the current name with the given web resource rules.
	 * The rules will be bundled into a separate package and then combined with the original
	 * using {@link WebResourcePackage#combine(WebResourcePackage, WebResourcePackage)}.
	 * The result will be registered under the original packaga name.
	 * <p>
	 * If the package with that name is not present to begin with, nothing will be done and {@code false} will be returned.
	 *
	 * @param name  of the package to extend
	 * @param rules to append to the package
	 * @return true if the package was present and has been extended, false if the package was not found and no changes have been made
	 */
	public boolean extendPackage( @NonNull String name, WebResourceRule... rules ) {
		return extendPackage( name, WebResourcePackage.of( rules ) );
	}

	/**
	 * Extend the package registered under the current name with the given web resource rules.
	 * The rules will be bundled into a separate package and then combined with the original
	 * using {@link WebResourcePackage#combine(WebResourcePackage, WebResourcePackage)}.
	 * The result will be registered under the original packaga name.
	 * <p>
	 * If the package with that name is not present to begin with, nothing will be done and {@code false} will be returned.
	 *
	 * @param name  of the package to extend
	 * @param rules to append to the package
	 * @return true if the package was present and has been extended, false if the package was not found and no changes have been made
	 */
	public boolean extendPackage( @NonNull String name, @NonNull Collection<WebResourceRule> rules ) {
		return extendPackage( name, WebResourcePackage.of( rules ) );
	}

	/**
	 * Extend the package registered under the current name with the given package.
	 * This will effectively call {@link WebResourcePackage#combine(WebResourcePackage, WebResourcePackage)} with both packages and
	 * register the result under the same name.
	 * <p>
	 * If the package with that name is not present to begin with, nothing will be done and {@code false} will be returned.
	 *
	 * @param name      of the package to extend
	 * @param extension to append to the package
	 * @return true if the package was present and has been extended, false if the package was not found and no changes have been made
	 */
	public boolean extendPackage( @NonNull String name, @NonNull WebResourcePackage extension ) {
		WebResourcePackage original = getPackage( name );

		if ( original != null ) {
			packages.put( name, WebResourcePackage.combine( original, extension ) );
			return true;
		}

		return false;
	}
}
