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
import com.foreach.across.core.context.module.AcrossModuleDependencySorter.DependencySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.Collection;

import static com.foreach.across.core.context.module.AcrossModuleDependencySorter.DependencySpec.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class TestAcrossModuleDependencySorter
{
	@Test
	@DisplayName("initial order is kept if no dependencies")
	void registrationOrder() {
		DependencySpec a = spec().name( "a" ).build();
		DependencySpec b = spec().name( "b" ).build();
		DependencySpec c = spec().name( "c" ).build();

		assertSorted( Arrays.asList( a, b, c ), Arrays.asList( a, b, c ) );
	}

	@Test
	void sortByRole() {
		DependencySpec a = spec().name( "a" ).role( AcrossModuleRole.APPLICATION ).build();
		DependencySpec b = spec().name( "b" ).role( AcrossModuleRole.APPLICATION ).build();
		DependencySpec c = spec().name( "c" ).role( AcrossModuleRole.INFRASTRUCTURE ).build();
		DependencySpec d = spec().name( "d" ).role( AcrossModuleRole.INFRASTRUCTURE ).build();
		DependencySpec e = spec().name( "e" ).role( AcrossModuleRole.POSTPROCESSOR ).build();
		DependencySpec f = spec().name( "f" ).role( AcrossModuleRole.POSTPROCESSOR ).build();

		assertSorted( Arrays.asList( a, b ), Arrays.asList( a, b ) );
		assertSorted( Arrays.asList( b, a ), Arrays.asList( b, a ) );
		assertSorted( Arrays.asList( c, d, a, b, e, f ), Arrays.asList( c, d, a, b, e, f ) );
		assertSorted( Arrays.asList( d, c, b, a, f, e ), Arrays.asList( d, c, b, a, f, e ) );

		assertSorted( Arrays.asList( a, b, c ), Arrays.asList( c, a, b ) );
		assertSorted( Arrays.asList( a, c, e ), Arrays.asList( c, a, e ) );
		assertSorted( Arrays.asList( e, a, c ), Arrays.asList( c, a, e ) );
	}

	@Test
	void sortByOrderInRole() {
		DependencySpec a = spec().name( "a" ).build();
		DependencySpec b = spec().name( "b" ).build();
		DependencySpec c = spec().name( "c" ).orderInRole( -1 ).build();
		DependencySpec d = spec().name( "d" ).orderInRole( Ordered.HIGHEST_PRECEDENCE ).build();
		DependencySpec e = spec().name( "e" ).orderInRole( 1 ).build();
		DependencySpec f = spec().name( "f" ).orderInRole( Ordered.LOWEST_PRECEDENCE ).build();

		assertSorted( Arrays.asList( a, b ), Arrays.asList( a, b ) );
		assertSorted( Arrays.asList( b, a ), Arrays.asList( b, a ) );
		assertSorted( Arrays.asList( a, c ), Arrays.asList( c, a ) );
		assertSorted( Arrays.asList( c, a ), Arrays.asList( c, a ) );
		assertSorted( Arrays.asList( a, c, e ), Arrays.asList( c, a, e ) );
		assertSorted( Arrays.asList( a, e, c ), Arrays.asList( c, a, e ) );
		assertSorted( Arrays.asList( a, b, c, d, e, f ), Arrays.asList( d, c, a, b, e, f ) );
		assertSorted( Arrays.asList( f, e, b, a, c, d ), Arrays.asList( d, c, b, a, e, f ) );
		assertSorted( Arrays.asList( f, a, e, c, b, d ), Arrays.asList( d, c, a, b, e, f ) );
	}

	@Test
	void sortByRoleAndOrderInRole() {
		DependencySpec a = spec().name( "a" ).role( AcrossModuleRole.APPLICATION ).orderInRole( Ordered.LOWEST_PRECEDENCE ).build();
		DependencySpec b = spec().name( "b" ).role( AcrossModuleRole.APPLICATION ).orderInRole( Ordered.HIGHEST_PRECEDENCE ).build();
		DependencySpec c = spec().name( "c" ).role( AcrossModuleRole.INFRASTRUCTURE ).orderInRole( -4 ).build();
		DependencySpec d = spec().name( "d" ).role( AcrossModuleRole.INFRASTRUCTURE ).orderInRole( -3 ).build();
		DependencySpec e = spec().name( "e" ).role( AcrossModuleRole.POSTPROCESSOR ).orderInRole( -2 ).build();
		DependencySpec f = spec().name( "f" ).role( AcrossModuleRole.POSTPROCESSOR ).orderInRole( -1 ).build();

		assertSorted( Arrays.asList( a, b, c, d, e, f ), Arrays.asList( c, d, b, a, e, f ) );
		assertSorted( Arrays.asList( f, e, d, c, b, a ), Arrays.asList( c, d, b, a, e, f ) );
		assertSorted( Arrays.asList( c, d, b, a, e, f ), Arrays.asList( c, d, b, a, e, f ) );
	}

	@Test
	void sortBySimpleRequiredDependencies() {
		DependencySpec a = spec().name( "a" ).name( "aa" ).build();
		DependencySpec b = spec().name( "b" ).requiredDependency( "a" ).build();
		DependencySpec c = spec().name( "c" ).requiredDependency( "b" ).build();
		DependencySpec d = spec().name( "d" ).requiredDependency( "aa" ).build();

		assertSorted( Arrays.asList( b, a ), Arrays.asList( a, b ) );
		assertSorted( Arrays.asList( b, c ), Arrays.asList( b, c ) );
		assertSorted( Arrays.asList( c, b ), Arrays.asList( b, c ) );
		assertSorted( Arrays.asList( b, c, a ), Arrays.asList( a, b, c ) );
		assertSorted( Arrays.asList( c, b, a ), Arrays.asList( a, b, c ) );
		assertSorted( Arrays.asList( d, c, b, a ), Arrays.asList( a, d, b, c ) );
		assertSorted( Arrays.asList( a, d, b, c ), Arrays.asList( a, d, b, c ) );
	}

	@Test
	void sortByMultipleRequiredDependencies() {
		DependencySpec a = spec().name( "a" ).requiredDependency( "d" ).build();
		DependencySpec b = spec().name( "b" ).requiredDependency( "a" ).requiredDependency( "c" ).build();
		DependencySpec c = spec().name( "c" ).requiredDependency( "d" ).build();
		DependencySpec d = spec().name( "d" ).build();
		DependencySpec e = spec().name( "e" ).requiredDependency( "a" ).requiredDependency( "b" ).requiredDependency( "c" ).build();

		assertSorted( Arrays.asList( a, b, c, d, e ), Arrays.asList( d, a, c, b, e ) );
		assertSorted( Arrays.asList( e, a, b, c, d ), Arrays.asList( d, a, c, b, e ) );
		assertSorted( Arrays.asList( e, d, c, b, a ), Arrays.asList( d, c, a, b, e ) );
		assertSorted( Arrays.asList( c, b, e, a, d ), Arrays.asList( d, c, a, b, e ) );

		// correct order is kept
		assertSorted( Arrays.asList( d, a, c, b, e ), Arrays.asList( d, a, c, b, e ) );
		assertSorted( Arrays.asList( c, b, e, a, d ), Arrays.asList( d, c, a, b, e ) );
	}

	@Test
	void sortBySimpleOptionalDependencies() {
		DependencySpec a = spec().name( "a" ).name( "aa" ).build();
		DependencySpec b = spec().name( "b" ).optionalDependency( "a" ).build();
		DependencySpec c = spec().name( "c" ).optionalDependency( "b" ).build();
		DependencySpec d = spec().name( "d" ).optionalDependency( "aa" ).build();

		assertSorted( Arrays.asList( b, a ), Arrays.asList( a, b ) );
		assertSorted( Arrays.asList( b, c ), Arrays.asList( b, c ) );
		assertSorted( Arrays.asList( c, b ), Arrays.asList( b, c ) );
		assertSorted( Arrays.asList( b, c, a ), Arrays.asList( a, b, c ) );
		assertSorted( Arrays.asList( c, b, a ), Arrays.asList( a, b, c ) );
		assertSorted( Arrays.asList( d, c, b, a ), Arrays.asList( a, d, b, c ) );
		assertSorted( Arrays.asList( a, d, b, c ), Arrays.asList( a, d, b, c ) );
	}

	@Test
	void sortByMultipleOptionalDependencies() {
		DependencySpec a = spec().name( "a" ).optionalDependency( "d" ).build();
		DependencySpec b = spec().name( "b" ).optionalDependency( "a" ).optionalDependency( "c" ).build();
		DependencySpec c = spec().name( "c" ).optionalDependency( "d" ).build();
		DependencySpec d = spec().name( "d" ).build();
		DependencySpec e = spec().name( "e" ).optionalDependency( "a" ).optionalDependency( "b" ).optionalDependency( "c" ).build();

		assertSorted( Arrays.asList( a, b, c, d, e ), Arrays.asList( d, a, c, b, e ) );
		assertSorted( Arrays.asList( e, a, b, c, d ), Arrays.asList( d, a, c, b, e ) );
		assertSorted( Arrays.asList( e, d, c, b, a ), Arrays.asList( d, c, a, b, e ) );
		assertSorted( Arrays.asList( c, b, e, a, d ), Arrays.asList( d, c, a, b, e ) );

		// correct order is kept
		assertSorted( Arrays.asList( d, a, c, b, e ), Arrays.asList( d, a, c, b, e ) );
		assertSorted( Arrays.asList( c, b, e, a, d ), Arrays.asList( d, c, a, b, e ) );
	}

	@Test
	void requiredDependencyTakesPrecedenceOverOptional() {
		DependencySpec a = spec().name( "a" ).name( "aa" ).build();
		DependencySpec b = spec().name( "b" ).name( "bb" ).optionalDependency( "d" ).build();
		DependencySpec c = spec().name( "c" ).optionalDependency( "aa" ).requiredDependency( "bb" ).build();
		DependencySpec d = spec().name( "d" ).optionalDependency( "e" ).build();
		DependencySpec e = spec().name( "e" ).requiredDependency( "f" ).build();
		DependencySpec f = spec().name( "f" ).requiredDependency( "d" ).build();

		assertSorted( Arrays.asList( d, e, f ), Arrays.asList( d, f, e ) );
		assertSorted( Arrays.asList( f, e, d ), Arrays.asList( d, f, e ) );

		assertSorted( Arrays.asList( a, b, c ), Arrays.asList( a, b, c ) );
		assertSorted( Arrays.asList( c, b, a ), Arrays.asList( b, a, c ) );
		assertSorted( Arrays.asList( c, a, b ), Arrays.asList( a, b, c ) );

		assertSorted( Arrays.asList( a, b, c, d, e, f ), Arrays.asList( a, d, f, e, b, c ) );
		assertSorted( Arrays.asList( f, e, d, c, b, a ), Arrays.asList( d, f, e, b, a, c ) );
	}

	@Test
	void complexSetup() {
		DependencySpec a = spec().name( "a" ).role( AcrossModuleRole.INFRASTRUCTURE ).orderInRole( Ordered.HIGHEST_PRECEDENCE + 1000 ).build();
		DependencySpec b = spec().name( "b" ).role( AcrossModuleRole.POSTPROCESSOR ).orderInRole( Ordered.LOWEST_PRECEDENCE - 1000 ).build();
		DependencySpec c = spec().name( "c" ).requiredDependency( "d" ).build();
		DependencySpec d = spec().name( "d" ).optionalDependency( "e" ).build();
		DependencySpec e = spec().name( "e" ).role( AcrossModuleRole.INFRASTRUCTURE ).orderInRole( Ordered.HIGHEST_PRECEDENCE ).build();
		DependencySpec f = spec().name( "f" ).role( AcrossModuleRole.POSTPROCESSOR ).orderInRole( Ordered.LOWEST_PRECEDENCE ).build();
		DependencySpec g = spec().name( "g" ).optionalDependency( "f" ).requiredDependency( "b" ).build();

		assertSorted( Arrays.asList( a, b, c, d ), Arrays.asList( a, d, c, b ) );
		assertSorted( Arrays.asList( a, b, c, d, e, f ), Arrays.asList( e, a, d, c, b, f ) );
		assertSorted( Arrays.asList( f, e, d, c, b, a ), Arrays.asList( e, a, d, c, b, f ) );
		assertSorted( Arrays.asList( a, b, c, d, e, f, g ), Arrays.asList( e, a, d, c, b, f, g ) );
	}

	@Test
	@DisplayName("AX-229 - invalid bootstrap order")
	void walkDependenciesOfSameTypeInPreviousOrder() {
		DependencySpec a = spec().name( "a" ).requiredDependencies( Arrays.asList( "b", "c", "d" ) ).build();
		DependencySpec b = spec().name( "b" ).optionalDependencies( Arrays.asList( "c", "d" ) ).build();
		DependencySpec c = spec().name( "c" ).build();
		DependencySpec d = spec().name( "d" ).build();

		assertSorted( Arrays.asList( d, c, b, a ), Arrays.asList( d, c, b, a ) );
		assertSorted( Arrays.asList( d, a, c, b ), Arrays.asList( d, c, b, a ) );
		assertSorted( Arrays.asList( a, b, c, d ), Arrays.asList( c, d, b, a ) );
	}

	@Test
	@DisplayName("dependencySpecResolver function")
	void simpleSort( @Mock AcrossModuleDescriptor moduleOne, @Mock AcrossModuleDescriptor moduleTwo ) {
		when( moduleOne.toDependencySpec() ).thenReturn( builder().name( "one" ).role( AcrossModuleRole.APPLICATION ).orderInRole( 0 ).build() );
		when( moduleTwo.toDependencySpec() ).thenReturn( builder().name( "two" ).role( AcrossModuleRole.APPLICATION ).orderInRole( 0 )
		                                                          .requiredDependency( "one" ).build() );

		Collection<AcrossModuleDescriptor> descriptors
				= AcrossModuleDependencySorter.sort( Arrays.asList( moduleTwo, moduleOne ), AcrossModuleDescriptor::toDependencySpec );
		assertThat( descriptors ).isEqualTo( Arrays.asList( moduleOne, moduleTwo ) );
	}

	private DependencySpec.DependencySpecBuilder spec() {
		return DependencySpec.builder().role( AcrossModuleRole.APPLICATION ).orderInRole( 0 );
	}

	private void assertSorted( Collection<DependencySpec> input, Collection<DependencySpec> output ) {
		assertThat( new AcrossModuleDependencySorter( input ).sort() ).isEqualTo( output );
	}
}
