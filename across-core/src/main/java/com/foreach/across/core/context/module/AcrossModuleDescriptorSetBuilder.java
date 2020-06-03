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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.bootstrap.ModuleDependencyMissingException;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for building the entire set of {@link AcrossModuleDescriptor} instances for a defined
 * collection of modules. This will resolve all dependencies using a specified {@link #dependencyResolver(ModuleDependencyResolver)}.
 * An exception will be thrown if a required dependency is missing, a missing optional dependency will
 * silently be ignored.
 *
 * @author Arne Vandamme
 * @see AcrossModuleDescriptor
 * @since 5.0.0
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public final class AcrossModuleDescriptorSetBuilder
{
	/**
	 * Resolver that can be used for looking up modules by name.
	 */
	@Setter
	@Nullable
	private ModuleDependencyResolver dependencyResolver;

	/**
	 * Build the actual collection of module descriptors that should be loaded.
	 * This will attempt to resolve all required modules and will throw an exception if a
	 * required module cannot be found.
	 *
	 * @param sourceModules collection of modules that should have its
	 * @return initial collection of modules
	 */
	public Collection<AcrossModuleDescriptor> build( @NonNull Collection<AcrossModule> sourceModules ) {
		Map<String, AcrossModuleDescriptor> descriptorMap = new HashMap<>();

		sourceModules.forEach( m -> descriptorMap.put( m.getName(), AcrossModuleDescriptor.from( m ) ) );

		Deque<AcrossModuleDescriptor> queue = new ArrayDeque<>( descriptorMap.size() * 2 );
		queue.addAll( descriptorMap.values() );

		while ( !queue.isEmpty() ) {
			AcrossModuleDescriptor descriptor = queue.removeFirst();
			String moduleName = descriptor.getModuleName();

			descriptor.getRequiredModules()
			          .forEach( requiredModuleName -> {
				          if ( !descriptorMap.containsKey( requiredModuleName ) ) {
					          AcrossModuleDescriptor dependencyDescriptor = resolveModule( requiredModuleName, true )
							          .map( AcrossModuleDescriptor::from )
							          .orElseThrow( () -> new ModuleDependencyMissingException( moduleName, requiredModuleName ) );

					          LOG.trace( "Resolved required dependency {} for module {}", requiredModuleName, moduleName );
					          descriptorMap.put( dependencyDescriptor.getModuleName(), dependencyDescriptor );
					          queue.add( dependencyDescriptor );
				          }
				          else {
					          LOG.trace( "Required dependency {} for module {} satisfied - was previously registered", requiredModuleName, moduleName );
				          }
			          } );

			descriptor.getOptionalModules()
			          .forEach( optionalModuleName -> {
				          if ( !descriptorMap.containsKey( optionalModuleName ) ) {
					          AcrossModuleDescriptor dependencyDescriptor = resolveModule( optionalModuleName, false )
							          .map( AcrossModuleDescriptor::from )
							          .orElse( null );

					          if ( dependencyDescriptor != null ) {
						          LOG.trace( "Resolved optional dependency {} for module {}", optionalModuleName, moduleName );
						          descriptorMap.put( dependencyDescriptor.getModuleName(), dependencyDescriptor );
						          queue.add( dependencyDescriptor );
					          }
					          else {
						          LOG.trace( "{} is missing optional dependency {}", moduleName, optionalModuleName );
						          descriptorMap.put( optionalModuleName, null );
					          }
				          }
				          else {
					          LOG.trace( "Optional dependency {} for module {} satisfied - was previously registered", optionalModuleName, moduleName );
				          }
			          } );
		}

		return descriptorMap.values()
		                    .stream()
		                    .filter( Objects::nonNull )
		                    .collect( Collectors.toSet() );
	}

	private Optional<AcrossModule> resolveModule( String moduleName, boolean required ) {
		if ( dependencyResolver != null ) {
			return dependencyResolver.resolveModule( moduleName, required );
		}
		return Optional.empty();
	}
}
