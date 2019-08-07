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
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.bootstrap.ModuleDependencyMissingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@DisplayName( "Module descriptor set building" )
@ExtendWith(MockitoExtension.class)
class TestAcrossModuleDescriptorSetBuilder
{
	private final AcrossModule moduleOne = new ModuleOne();
	private final AcrossModule moduleTwo = new ModuleTwo();
	private final AcrossModule moduleThree = new ModuleThree();
	private final AcrossModule moduleFour = new ModuleFour();
	private final AcrossModuleDescriptor moduleOneDescriptor = AcrossModuleDescriptor.from( new ModuleOne() );
	private final AcrossModuleDescriptor moduleTwoDescriptor = AcrossModuleDescriptor.from( new ModuleTwo() );
	private final AcrossModuleDescriptor moduleThreeDescriptor = AcrossModuleDescriptor.from( new ModuleThree() );
	private final AcrossModuleDescriptor moduleFourDescriptor = AcrossModuleDescriptor.from( new ModuleFour() );

	private AcrossModuleDescriptorSetBuilder builder = new AcrossModuleDescriptorSetBuilder();

	@Test
	void emptyCollection() {
		assertThat( builder.build( Collections.emptyList() ) ).isEmpty();
	}

	@Test
	void noDependencies() {
		assertThat( builder.build( Collections.singleton( moduleTwo ) ) )
				.containsExactly( moduleTwoDescriptor );
	}

	@Test
	void allDependenciesInSource() {
		assertThat( builder.build( Arrays.asList( moduleOne, moduleTwo ) ) )
				.containsExactlyInAnyOrder( moduleOneDescriptor, moduleTwoDescriptor );
	}

	@Test
	void requiredDependencyNotFoundDueToNoResolver() {
		assertThatExceptionOfType( ModuleDependencyMissingException.class )
				.isThrownBy( () -> builder.build( Collections.singleton( moduleOne ) ) )
				.matches( e -> e.getModuleName().equals( moduleOne.getName() ) )
				.matches( e -> e.getDependencyName().equals( moduleTwo.getName() ) );
	}

	@Test
	void requiredDependencyNotFound( @Mock ModuleDependencyResolver dependencyResolver ) {
		assertThatExceptionOfType( ModuleDependencyMissingException.class )
				.isThrownBy( () -> builder.dependencyResolver( dependencyResolver ).build( Collections.singleton( moduleOne ) ) )
				.matches( e -> e.getModuleName().equals( moduleOne.getName() ) )
				.matches( e -> e.getDependencyName().equals( moduleTwo.getName() ) );

		verify( dependencyResolver ).resolveModule( moduleTwo.getName(), true );
		verifyNoMoreInteractions( dependencyResolver );
	}

	@Test
	@DisplayName("required found - optional missing")
	void optionalDependencyNotFound( @Mock ModuleDependencyResolver dependencyResolver ) {
		when( dependencyResolver.resolveModule( moduleTwo.getName(), true ) ).thenReturn( Optional.of( moduleTwo ) );

		assertThat( builder.dependencyResolver( dependencyResolver ).build( Collections.singleton( moduleOne ) ) )
				.containsExactlyInAnyOrder( moduleOneDescriptor, moduleTwoDescriptor );

		verify( dependencyResolver ).resolveModule( moduleThree.getName(), false );
	}

	@Test
	void optionalDependencyFound( @Mock ModuleDependencyResolver dependencyResolver ) {
		doReturn( Optional.of( moduleTwo ) ).when( dependencyResolver ).resolveModule( moduleTwo.getName(), true );
		doReturn( Optional.of( moduleThree ) ).when( dependencyResolver ).resolveModule( moduleThree.getName(), false );

		assertThat( builder.dependencyResolver( dependencyResolver ).build( Collections.singleton( moduleOne ) ) )
				.containsExactlyInAnyOrder( moduleOneDescriptor, moduleTwoDescriptor, moduleThreeDescriptor );
	}

	@Test
	@DisplayName("dependencies added transitively")
	void transitiveDependencies( @Mock ModuleDependencyResolver dependencyResolver ) {
		doReturn( Optional.of( moduleTwo ) ).when( dependencyResolver ).resolveModule( moduleTwo.getName(), true );
		doReturn( Optional.of( moduleOne ) ).when( dependencyResolver ).resolveModule( moduleOne.getName(), true );

		assertThat( builder.dependencyResolver( dependencyResolver ).build( Collections.singleton( moduleFour ) ) )
				.containsExactlyInAnyOrder( moduleOneDescriptor, moduleTwoDescriptor, moduleFourDescriptor );

		verify( dependencyResolver ).resolveModule( moduleThree.getName(), false );
	}

	@AcrossDepends(required = "ModuleTwo", optional = "ModuleThree")
	public static class ModuleOne extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleOne";
		}
	}

	public static class ModuleTwo extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleTwo";
		}
	}

	public static class ModuleThree extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleThree";
		}
	}

	@AcrossDepends(required = "ModuleOne")
	public static class ModuleFour extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleFour";
		}
	}
}
