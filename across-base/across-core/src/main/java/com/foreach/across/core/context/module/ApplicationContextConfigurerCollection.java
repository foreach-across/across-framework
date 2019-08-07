/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.module;

import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the actual collection of {@link com.foreach.across.core.context.configurer.ApplicationContextConfigurer}
 * that should be loaded for a {@link AcrossModuleBootstrapConfiguration} and its extensions.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ApplicationContextConfigurerCollection implements Iterable<ApplicationContextConfigurer>
{
	@NonNull
	private Collection<ApplicationContextConfigurer> configurers;

	@Override
	public Iterator<ApplicationContextConfigurer> iterator() {
		return configurers.iterator();
	}

	/**
	 * @return true if there is at least one non-optional configurer that has components
	 */
	public boolean hasComponents() {
		return configurers.stream().anyMatch( ac -> !ac.isOptional() && ac.hasComponents() );
	}

	/**
	 * Build the collection of configurers for a module bootstrap configuration.
	 * This will apply all properties and remove all explicitly excluded classes.
	 *
	 * @param configuration bootstrap configuration
	 * @return collection
	 */
	public static ApplicationContextConfigurerCollection from( @NonNull AcrossModuleBootstrapConfiguration configuration ) {
		List<ApplicationContextConfigurer> configurerList = new ArrayList<>();

		return new ApplicationContextConfigurerCollection( configurerList );
	}
}
