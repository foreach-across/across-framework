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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Converts a set of {@link AcrossModuleDescriptor}s into the actual {@link AcrossModuleBootstrapConfiguration}s that
 * should be used to startup the module context. Ensures that extension modules are merged into the first available
 * target configuration.
 * <p/>
 * Provides direct access to the configuration for a specific module, using {@link #getConfigurationForModule(String)}.
 * This will always return the module specific configuration, even for an extension module. It will also take all module
 * aliases into account as such multiple module names may return the same configuration instance.
 * <p/>
 * Use {@link #getConfigurationsInOrder()} for retrieving the ordered list of configurations that should be bootstrapped.
 *
 * @author Arne Vandamme
 * @see AcrossModuleDependencySorter
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AcrossModuleBootstrapConfigurationSet
{
	private final Collection<AcrossModuleBootstrapConfiguration> configurationsInBootstrapOrder;
	private final Map<String, AcrossModuleBootstrapConfiguration> configurationsByModuleName;

	/**
	 * @return ordered collection of the the configurations that should be bootstrapped
	 */
	public Collection<AcrossModuleBootstrapConfiguration> getConfigurationsInOrder() {
		return configurationsInBootstrapOrder;
	}

	/**
	 * Find the configuration attached to a particular module name. This can be the primary module name
	 * or any of its aliases that might have been configured.
	 *
	 * @param moduleName name of the module
	 * @return configuration when module is present
	 */
	public Optional<AcrossModuleBootstrapConfiguration> getConfigurationForModule( @NonNull String moduleName ) {
		return Optional.ofNullable( configurationsByModuleName.get( moduleName ) );
	}

	/**
	 * Build the collection of {@link AcrossModuleBootstrapConfiguration} instances for the specified list of module descriptors.
	 * This will convert the descriptors to configuration instances, order and merge the extensions together in order.
	 * Extensions will be added to the target module according to their dependency order as well.
	 *
	 * @param moduleDescriptors original set of descriptors
	 * @return configuration set
	 */
	public static AcrossModuleBootstrapConfigurationSet create( @NonNull Collection<AcrossModuleDescriptor> moduleDescriptors ) {
		// do an initial sort on the module descriptors
		Collection<AcrossModuleDescriptor> descriptors = AcrossModuleDependencySorter.sort( moduleDescriptors, AcrossModuleDescriptor::toDependencySpec );

		// build configurations
		Map<String, AcrossModuleBootstrapConfiguration> configurationsByModuleName = new HashMap<>();
		Set<AcrossModuleBootstrapConfiguration> configurations = new HashSet<>();

		descriptors.stream()
		           .map( AcrossModuleBootstrapConfiguration::from )
		           .forEach( configuration -> {
			           configurations.add( configuration );

			           AcrossModuleDescriptor moduleDescriptor = configuration.getModuleDescriptor();
			           configurationsByModuleName.put( moduleDescriptor.getModuleName(), configuration );
			           moduleDescriptor.getModuleNameAliases().forEach( alias -> configurationsByModuleName.put( alias, configuration ) );
		           } );

		// merge the extension configurations to the first available target
		mergeExtensions( configurations, configurationsByModuleName );

		// sort the remaining configurations, modified dependencies have changed
		Collection<AcrossModuleBootstrapConfiguration> sorted
				= AcrossModuleDependencySorter.sort( configurations, AcrossModuleBootstrapConfiguration::toDependencySpec );

		return new AcrossModuleBootstrapConfigurationSet( Collections.unmodifiableCollection( sorted ),
		                                                  Collections.unmodifiableMap( configurationsByModuleName ) );
	}

	private static void mergeExtensions( Collection<AcrossModuleBootstrapConfiguration> configurations,
	                                     Map<String, AcrossModuleBootstrapConfiguration> configurationsByModuleName ) {
		for ( Iterator<AcrossModuleBootstrapConfiguration> it = configurations.iterator(); it.hasNext(); ) {
			AcrossModuleBootstrapConfiguration configuration = it.next();
			AcrossModuleDescriptor moduleDescriptor = configuration.getModuleDescriptor();

			if ( moduleDescriptor.isExtensionModule() ) {
				it.remove();

				Optional<AcrossModuleBootstrapConfiguration> target = moduleDescriptor.getExtensionTargets()
				                                                                      .stream()
				                                                                      .map( configurationsByModuleName::get )
				                                                                      .filter( Objects::nonNull )
				                                                                      .findFirst();
				if ( !target.isPresent() ) {
					LOG.warn( "Ignoring module {} as none of the target modules were present, expected one of: {}",
					          moduleDescriptor.getModuleName(), moduleDescriptor.getExtensionTargets() );
				}
				else {
					target.get().addExtension( configuration );
				}
			}
		}
	}
}
