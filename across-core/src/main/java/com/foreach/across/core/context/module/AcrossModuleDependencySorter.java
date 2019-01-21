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
import lombok.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@NotThreadSafe
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AcrossModuleDependencySorter
{
	private final Collection<DependencySpec> dependencySpecs;

	private Map<String, DependencySpec> specsByName;
	private LinkedList<DependencySpec> sorted;

	public Collection<DependencySpec> sort() {
		specsByName = new HashMap<>();
		dependencySpecs.forEach( spec -> spec.getNames().forEach( n -> specsByName.put( n, spec ) ) );

		sorted = new LinkedList<>( dependencySpecs );

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

	public static <U> Collection<U> sort( @NonNull Collection<U> targets, @NonNull Function<U, DependencySpec> dependencySpecResolver ) {
		Map<DependencySpec, U> map = new LinkedHashMap<>();
		targets.forEach( target -> map.put( dependencySpecResolver.apply( target ), target ) );

		return new AcrossModuleDependencySorter( map.keySet() )
				.sort()
				.stream()
				.map( map::get )
				.collect( Collectors.toList() );
	}

	@Builder
	@Getter
	@EqualsAndHashCode
	static class DependencySpec
	{
		@NonNull
		private final AcrossModuleRole role;

		private final int orderInRole;

		@Singular
		private final Set<String> names;

		@Singular
		private final Set<String> requiredDependencies;

		@Singular
		private final Set<String> optionalDependencies;

		@Override
		public String toString() {
			return Objects.toString( names );
		}

		private long getModulePriority() {
			return role.asPriority( orderInRole );
		}
	}
}
