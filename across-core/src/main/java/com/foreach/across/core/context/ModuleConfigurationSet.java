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

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a collection of classes annotated with {@link @ModuleConfiguration},
 * mapped on the module names they apply for.
 *
 * @author Arne Vandamme
 */
public class ModuleConfigurationSet
{
	private final Map<Class<?>, ModuleConfigurationScope> scopedAnnotatedClasses = new LinkedHashMap<>();

	/**
	 * Get the annotated classes registered to the module specified by name.
	 * Optionally a number of module name aliases can be specified - usually used by dynamic modules.
	 */
	public Class<?>[] getAnnotatedClasses( String moduleName, String... aliases ) {
		String[] moduleNames = ArrayUtils.addAll( aliases, moduleName );
		List<Class<?>> annotatedClasses = new ArrayList<>();
		scopedAnnotatedClasses.forEach( ( annotatedClass, modules ) -> {
			if ( !containsAny( modules.excludedModules, moduleNames )
					&& ( modules.addToAll || containsAny( modules.includedModules, moduleNames ) ) ) {
				annotatedClasses.add( annotatedClass );
			}
		} );

		return annotatedClasses.toArray( new Class[annotatedClasses.size()] );
	}

	private boolean containsAny( Set<String> moduleNameSet, String... moduleName ) {
		return Stream.of( moduleName ).anyMatch( moduleNameSet::contains );
	}

	/**
	 * Register an annotated class to be added to all modules.
	 * Any additional {@link #register(Class, String...)} calls will have no effect as this module will be added
	 * for all anyway.
	 *
	 * @param annotatedClass configuration
	 */
	public void register( Class<?> annotatedClass ) {
		ModuleConfigurationScope scope
				= scopedAnnotatedClasses.getOrDefault( annotatedClass, new ModuleConfigurationScope() );
		scope.addToAll = true;

		scopedAnnotatedClasses.put( annotatedClass, scope );
	}

	/**
	 * Register an annotated class to be added to a specific set of modules.
	 *
	 * @param annotatedClass configuration
	 * @param moduleNames    names of the modules to which this configuration should be added
	 */
	public void register( Class<?> annotatedClass, String... moduleNames ) {
		ModuleConfigurationScope scope
				= scopedAnnotatedClasses.getOrDefault( annotatedClass, new ModuleConfigurationScope() );
		Collections.addAll( scope.includedModules, moduleNames );

		scopedAnnotatedClasses.put( annotatedClass, scope );
	}

	/**
	 * Register an annotated class to be excluded specifically for a number of modules.
	 *
	 * @param annotatedClass configuration to be excluded
	 * @param moduleNames    names of the modules to which this configuration should <strong>never</strong> be added
	 */
	public void exclude( Class<?> annotatedClass, String... moduleNames ) {
		ModuleConfigurationScope scope
				= scopedAnnotatedClasses.getOrDefault( annotatedClass, new ModuleConfigurationScope() );
		Collections.addAll( scope.excludedModules, moduleNames );

		scopedAnnotatedClasses.put( annotatedClass, scope );
	}

	/**
	 * Remove an annotated class from the set altogether.
	 *
	 * @param annotatedClass configuration
	 */
	public void remove( Class<?> annotatedClass ) {
		scopedAnnotatedClasses.remove( annotatedClass );
	}

	private static class ModuleConfigurationScope
	{
		public boolean addToAll = false;
		public final Set<String> includedModules = new HashSet<>();
		public final Set<String> excludedModules = new HashSet<>();
	}
}
