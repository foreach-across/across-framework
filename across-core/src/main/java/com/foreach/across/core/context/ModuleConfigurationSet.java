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
package com.foreach.across.core.context;

import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a collection of - usually {@link @ModuleConfiguration} - classes, mapped on the module names they apply for.
 * These classes will be registered in the module using an {@link org.springframework.context.annotation.ImportSelector}
 * in order to ensure correct conditional validation without premature class-loading. As such, only {@link Configuration}
 * annotated classes are correctly handled.
 *
 * @author Arne Vandamme
 * @see ClassPathScanningModuleConfigurationProvider
 */
public class ModuleConfigurationSet
{
	private final Map<String, ModuleConfigurationScope> scopedAnnotatedClasses = new LinkedHashMap<>();

	/**
	 * Get the configuration classes that should be added to the module specified by the name.
	 * Optionally a number of module name aliases can be specified - usually used by dynamic modules.
	 *
	 * @param moduleName of the module
	 * @param aliases    additional module names
	 * @return configurations
	 */
	public String[] getConfigurations( String moduleName, String... aliases ) {
		String[] moduleNames = ArrayUtils.addAll( aliases, moduleName );
		List<String> configurations = new ArrayList<>();
		scopedAnnotatedClasses.forEach( ( annotatedClass, modules ) -> {
			if ( !containsAny( modules.excludedModules, moduleNames )
					&& ( modules.addToAll || containsAny( modules.includedModules, moduleNames ) ) ) {
				configurations.add( annotatedClass );
			}
		} );

		return configurations.toArray( new String[configurations.size()] );
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
		String[] moduleNames = ArrayUtils.addAll( aliases, moduleName );
		List<String> configurations = new ArrayList<>();
		scopedAnnotatedClasses.forEach( ( annotatedClass, modules ) -> {
			if ( containsAny( modules.excludedModules, moduleNames ) ) {
				configurations.add( annotatedClass );
			}
		} );

		return configurations.toArray( new String[configurations.size()] );
	}

	private boolean containsAny( Set<String> moduleNameSet, String... moduleName ) {
		return Stream.of( moduleName ).anyMatch( moduleNameSet::contains );
	}

	/**
	 * Register a configuration class to be added to all modules.
	 * Any additional {@link #register(Class, String...)} calls will have no effect as this module will be added
	 * for all anyway.
	 *
	 * @param configurationClass configuration
	 */
	public void register( @NonNull Class<?> configurationClass ) {
		register( configurationClass.getName() );
	}

	/**
	 * Register a configuration class to be added to all modules.
	 * Any additional {@link #register(Class, String...)} calls will have no effect as this module will be added
	 * for all anyway.
	 *
	 * @param configurationClass configuration
	 */
	public void register( @NonNull String configurationClass ) {
		ModuleConfigurationScope scope = scopedAnnotatedClasses.getOrDefault( configurationClass, new ModuleConfigurationScope() );
		scope.addToAll = true;

		scopedAnnotatedClasses.put( configurationClass, scope );
	}

	/**
	 * Register a configuration class to be added to a specific set of modules.
	 *
	 * @param configurationClass configuration
	 * @param moduleNames        names of the modules to which this configuration should be added
	 */
	public void register( @NonNull Class<?> configurationClass, String... moduleNames ) {
		register( configurationClass.getName(), moduleNames );
	}

	/**
	 * Register a configuration class to be added to a specific set of modules.
	 *
	 * @param configurationClass configuration
	 * @param moduleNames        names of the modules to which this configuration should be added
	 */
	public void register( @NonNull String configurationClass, String... moduleNames ) {
		ModuleConfigurationScope scope = scopedAnnotatedClasses.getOrDefault( configurationClass, new ModuleConfigurationScope() );
		Collections.addAll( scope.includedModules, moduleNames );

		scopedAnnotatedClasses.put( configurationClass, scope );
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
		ModuleConfigurationScope scope
				= scopedAnnotatedClasses.getOrDefault( configurationClass, new ModuleConfigurationScope() );
		Collections.addAll( scope.excludedModules, moduleNames );

		scopedAnnotatedClasses.put( configurationClass, scope );
	}

	/**
	 * Remove a configuration class from the set altogether.
	 *
	 * @param configurationClass configuration
	 */
	public void remove( Class<?> configurationClass ) {
		remove( configurationClass.getName() );
	}

	/**
	 * Remove a configuration class from the set altogether.
	 *
	 * @param configurationClass configuration
	 */
	public void remove( String configurationClass ) {
		scopedAnnotatedClasses.remove( configurationClass );
	}

	private static class ModuleConfigurationScope
	{
		boolean addToAll = false;
		final Set<String> includedModules = new HashSet<>();
		final Set<String> excludedModules = new HashSet<>();
	}
}
