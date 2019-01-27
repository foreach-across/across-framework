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

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Slf4j
@Accessors(fluent = true, chain = true)
public class AcrossModuleBootstrapConfigurationSetBuilder
{
	/**
	 * Collection of descriptors for which the configurations should be built.
	 */
	@Setter
	private Collection<AcrossModuleDescriptor> moduleDescriptors;

	/**
	 * Build the collection of {@link AcrossModuleBootstrapConfiguration} instances for the specified {@link #moduleDescriptors}.
	 * This will convert the descriptors to configuration instances, order and merge the extensions together.
	 * Extensions will also be added in relative order.
	 * <p/>
	 * The result is the exact list of module configurations that should be bootstrapped.
	 *
	 * @return final
	 */
	public Collection<AcrossModuleBootstrapConfiguration> getConfigurationsInOrder() {
		// do an initial sort on the module descriptors
		Collection<AcrossModuleDescriptor> descriptors = AcrossModuleDependencySorter.sort( moduleDescriptors, AcrossModuleDescriptor::toDependencySpec );

		// build module configurations, merge in the extensions
		Collection<AcrossModuleBootstrapConfiguration> configurations = buildMergedConfigurations( descriptors );

		// sort the remaining configurations, modified dependencies have changed
		return AcrossModuleDependencySorter.sort( configurations, AcrossModuleBootstrapConfiguration::toDependencySpec );
	}

	private Collection<AcrossModuleBootstrapConfiguration> buildMergedConfigurations( Collection<AcrossModuleDescriptor> descriptors ) {
		Map<String, AcrossModuleBootstrapConfiguration> configurationsByName = new HashMap<>();
		Set<AcrossModuleBootstrapConfiguration> configurations = new HashSet<>();

		descriptors.stream()
		           .map( AcrossModuleBootstrapConfiguration::from )
		           .forEach( configuration -> {
			           configurations.add( configuration );

			           AcrossModuleDescriptor moduleDescriptor = configuration.getModuleDescriptor();
			           configurationsByName.put( moduleDescriptor.getModuleName(), configuration );
			           moduleDescriptor.getModuleNameAliases().forEach( alias -> configurationsByName.put( alias, configuration ) );
		           } );

		for ( Iterator<AcrossModuleBootstrapConfiguration> it = configurations.iterator(); it.hasNext(); ) {
			AcrossModuleBootstrapConfiguration configuration = it.next();
			AcrossModuleDescriptor moduleDescriptor = configuration.getModuleDescriptor();

			if ( moduleDescriptor.isExtensionModule() ) {
				it.remove();

				Optional<AcrossModuleBootstrapConfiguration> target = moduleDescriptor.getExtensionTargets()
				                                                                      .stream()
				                                                                      .map( configurationsByName::get )
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

		return configurations;
	}
}
