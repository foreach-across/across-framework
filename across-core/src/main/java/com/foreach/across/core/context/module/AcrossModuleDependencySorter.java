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

import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.CyclicModuleDependencyException;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapOrderBuilder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implements the sorting logic for Across modules. Sorting is done according to the following rules:
 * <ol>
 * <li>module role</li>
 * <li>order in module role</li>
 * <li>optional dependencies</li>
 * <li>required dependencies</li>
 * </ol>
 * Sorting is done in stages, to reach the most consistent ordering possible (eg: optional dependencies
 * are used as much as possible, and module role/order is also kept as much as possible, if required
 * dependencies do not explicitly impact is.
 * <p/>
 * This class is internal to the framework, called via {@link #sort(Collection, Function)}.
 * The inner class {@link DependencySpec} is used as container for all dependency specifications.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 * @see ModuleBootstrapOrderBuilder
 */
@NotThreadSafe
@Slf4j
class AcrossModuleDependencySorter
{
	private final Map<String, DependencySpec> specsByName = new HashMap<>();
	private final LinkedList<DependencySpec> sorted;

	AcrossModuleDependencySorter( Collection<DependencySpec> dependencySpecs ) {
		sorted = new LinkedList<>( dependencySpecs );
		dependencySpecs.forEach( spec -> spec.getNames().forEach( n -> specsByName.put( n, spec ) ) );
	}

	AcrossModuleDependencySorter verifyNoCyclicDependencies() {
		sorted.forEach( spec -> {
			Set<String> handled = new HashSet<>( spec.getNames() );
			verifyDependencies( handled, spec );
		} );
		return this;
	}

	private void verifyDependencies( Set<String> modulesHandled, DependencySpec spec ) {
		spec.getRequiredDependencies()
		    .stream()
		    .map( specsByName::get )
		    .forEach( dep -> {
			    dep.getRequiredDependencies().forEach( next -> {
				    if ( modulesHandled.contains( next ) ) {
					    LOG.error( "Unable to bootstrap, cyclic module dependencies detected: unable for module {} to depend back on {} (dependency name: {})",
					               spec.getNames(), specsByName.get( next ).getNames(), next );
					    throw new CyclicModuleDependencyException( next );
				    }
			    } );

			    modulesHandled.addAll( dep.getRequiredDependencies() );
			    verifyDependencies( modulesHandled, dep );
		    } );
	}

	Collection<DependencySpec> sort() {
		// sort according to module role and order in role
		sorted.sort( Comparator.comparingLong( DependencySpec::getModulePriority ) );

		// sort as if all dependencies are equal
		sortOnDependency( spec -> {
			LinkedHashSet<String> union = new LinkedHashSet<>( spec.getRequiredDependencies() );
			union.addAll( spec.getOptionalDependencies() );
			return union;
		} );

		// sort again on required dependencies to ensure they are respected over optional ones
		// this keeps the relative ordering as much as possible
		sortOnDependency( DependencySpec::getRequiredDependencies );

		return sorted;
	}

	private void sortOnDependency( Function<DependencySpec, Set<String>> dependencies ) {
		new ArrayList<>( sorted )
				.forEach(
						spec -> dependencies.apply( spec )
						                    .stream()
						                    .map( specsByName::get )
						                    .filter( Objects::nonNull )
						                    .distinct()
						                    .sorted( Comparator.comparingInt( sorted::indexOf ) )
						                    .forEach( dep -> {
							                    int currentPosition = sorted.indexOf( spec );
							                    int targetPos = sorted.indexOf( dep );

							                    if ( targetPos > currentPosition ) {
								                    sorted.add( currentPosition, sorted.remove( targetPos ) );
							                    }
						                    } )
				);
	}

	/**
	 * Sort a collection of objects according to their Across module dependency specifications.
	 * This will also verify that there are no cyclic dependencies in the required dependencies collection.
	 *
	 * @param targets                objects to sort
	 * @param dependencySpecResolver resolver to fetch a {@link DependencySpec} for a single object
	 * @param <U>                    object type
	 * @return original targets collection in resulting order
	 */
	public static <U> Collection<U> sort( @NonNull Collection<U> targets, @NonNull Function<U, DependencySpec> dependencySpecResolver ) {
		Map<DependencySpec, U> map = new LinkedHashMap<>();
		targets.forEach( target -> map.put( dependencySpecResolver.apply( target ), target ) );

		return new AcrossModuleDependencySorter( map.keySet() )
				.verifyNoCyclicDependencies()
				.sort()
				.stream()
				.map( map::get )
				.collect( Collectors.toList() );
	}

	/**
	 * Represents the different dependency and sorting related parameters.
	 */
	@Getter
	@EqualsAndHashCode
	static class DependencySpec
	{
		private final AcrossModuleRole role;
		private final int orderInRole;
		private final Set<String> names;
		private final Set<String> requiredDependencies;
		private final Set<String> optionalDependencies;

		@Builder
		public DependencySpec( @NonNull AcrossModuleRole role,
		                       int orderInRole,
		                       @NonNull @Singular Set<String> names,
		                       @NonNull @Singular Set<String> requiredDependencies,
		                       @NonNull @Singular Set<String> optionalDependencies ) {
			this.role = role;
			this.orderInRole = orderInRole;
			this.names = names;
			this.requiredDependencies = new LinkedHashSet<>( requiredDependencies );
			this.requiredDependencies.removeAll( names );
			this.optionalDependencies = new LinkedHashSet<>( optionalDependencies );
			this.optionalDependencies.removeAll( names );
		}

		@Override
		public String toString() {
			return Objects.toString( names );
		}

		private long getModulePriority() {
			return role.asPriority( orderInRole );
		}
	}
}
