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
package com.foreach.across.core.context;

import com.foreach.across.core.context.module.ModuleConfigurationExtension;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a collection of - usually {@link @ModuleConfiguration} - classes, mapped on the module names they apply for.
 * These classes will be registered in the module using an {@link org.springframework.context.annotation.ImportSelector}
 * in order to ensure correct conditional validation without premature class-loading. As such, only {@link Configuration}
 * annotated classes are correctly handled.
 *
 * @author Arne Vandamme
 * @see ClassPathScanningModuleConfigurationProvider
 * @see ModuleConfigurationExtension
 */
public class ModuleConfigurationSet
{
	private final Map<ModuleConfigurationExtension, Collection<String>> moduleExtensions = new LinkedHashMap<>();
	private final Map<String, Collection<String>> excludedClasses = new HashMap<>();

	/**
	 * Get the configuration classes that should be added to the module specified by the name.
	 * Optionally a number of module name aliases can be specified - usually used by dynamic modules.
	 *
	 * @param moduleName of the module
	 * @param aliases    additional module names
	 * @return configurations
	 */
	public ModuleConfigurationExtension[] getConfigurations( String moduleName, String... aliases ) {
		List<String> moduleNames = moduleNames( moduleName, aliases );

		return moduleExtensions.entrySet()
		                       .stream()
		                       .filter( e -> {
			                       Collection<String> modulesToInclude = e.getValue();
			                       Collection<String> excludedModules = excludedClasses.get( e.getKey().getAnnotatedClass() );

			                       if ( excludedModules != null ) {
				                       if ( excludedModules.isEmpty() || CollectionUtils.containsAny( excludedModules, moduleNames ) ) {
					                       return false;
				                       }
			                       }

			                       return modulesToInclude.isEmpty() || CollectionUtils.containsAny( modulesToInclude, moduleNames );
		                       } )
		                       .map( Map.Entry::getKey )
		                       .toArray( ModuleConfigurationExtension[]::new );
	}

	/**
	 * Get the explicitly excluded configuration classes registered to the module with that name
	 * Optionally a number of module name aliases can be specified - usually used by dynamic modules.
	 *
	 * @param moduleName of the module
	 * @param aliases    additional module names
	 * @return configurations
	 */
	public String[] getExcludedConfigurations( String moduleName, String... aliases ) {
		List<String> moduleNames = moduleNames( moduleName, aliases );

		return excludedClasses.entrySet()
		                      .stream()
		                      .filter( e -> e.getValue().isEmpty() || CollectionUtils.containsAny( e.getValue(), moduleNames ) )
		                      .map( Map.Entry::getKey )
		                      .toArray( String[]::new );
	}

	/**
	 * Register a configuration class to be added to all modules.
	 * Any additional {@link #register(ModuleConfigurationExtension, String...)} calls will have no effect as this module will be added
	 * for all anyway.
	 *
	 * @param extension configuration to be added
	 */
	public void register( @NonNull ModuleConfigurationExtension extension ) {
		moduleExtensions.put( extension, Collections.emptyList() );
	}

	/**
	 * Register a configuration class to be added to a specific set of modules.
	 *
	 * @param extension   configuration to be added
	 * @param moduleNames names of the modules to which this configuration should be added
	 */
	public void register( @NonNull ModuleConfigurationExtension extension, String... moduleNames ) {
		Collection<String> includedModules = moduleExtensions.get( extension );

		if ( includedModules == null ) {
			includedModules = new HashSet<>( moduleNames.length );
			moduleExtensions.put( extension, includedModules );
		}
		else if ( includedModules.isEmpty() ) {
			return;
		}

		includedModules.addAll( Arrays.asList( moduleNames ) );
	}

	private List<String> moduleNames( String moduleName, String[] aliases ) {
		List<String> moduleNames = new ArrayList<>( 1 + aliases.length );
		moduleNames.add( moduleName );
		moduleNames.addAll( Arrays.asList( aliases ) );
		return moduleNames;
	}

	/**
	 * Register a configuration class to be excluded specifically for a number of modules.
	 *
	 * @param configurationClass configuration to be excluded
	 * @param moduleNames        names of the modules to which this configuration should <strong>never</strong> be added
	 */
	public void exclude( @NonNull Class<?> configurationClass, String... moduleNames ) {
		exclude( configurationClass.getName(), moduleNames );
	}

	/**
	 * Register a configuration class to be excluded specifically for a number of modules.
	 *
	 * @param configurationClass configuration to be excluded
	 * @param moduleNames        names of the modules to which this configuration should <strong>never</strong> be added
	 */
	public void exclude( @NonNull String configurationClass, String... moduleNames ) {
		Collection<String> excludedModules = excludedClasses.get( configurationClass );

		if ( excludedModules == null ) {
			excludedModules = new HashSet<>( moduleNames.length );
			excludedClasses.put( configurationClass, excludedModules );
		}
		else if ( excludedModules.isEmpty() ) {
			return;
		}

		excludedModules.addAll( Arrays.asList( moduleNames ) );
	}

	/**
	 * Remove a configuration class from the set altogether.
	 *
	 * @param configurationClass configuration
	 */
	public void remove( @NonNull Class<?> configurationClass ) {
		remove( configurationClass.getName() );
	}

	/**
	 * Remove a configuration class from the set altogether.
	 * This will remove the class from both exclusions and registered extensions.
	 *
	 * @param configurationClass configuration
	 */
	public void remove( @NonNull String configurationClass ) {
		moduleExtensions.keySet()
		                .stream()
		                .filter( e -> configurationClass.equals( e.getAnnotatedClass() ) )
		                .collect( Collectors.toList() )
		                .forEach( moduleExtensions::remove );
		excludedClasses.remove( configurationClass );
	}
}
