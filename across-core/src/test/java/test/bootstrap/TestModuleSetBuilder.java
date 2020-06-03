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
package test.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.support.ModuleSet;
import com.foreach.across.core.context.support.ModuleSetBuilder;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
public class TestModuleSetBuilder
{
	private ModuleOne one = new ModuleOne();
	private ModuleTwo two = new ModuleTwo();
	private ModuleThree requiresOne = new ModuleThree();
	private ModuleFour requiresTwoOptionalOne = new ModuleFour();

	@Test
	public void empty() {
		ModuleSet moduleSet = new ModuleSetBuilder().build();

		assertTrue( moduleSet.getModules().isEmpty() );
		assertTrue( moduleSet.getModuleMap().isEmpty() );
	}

	@Test
	public void moduleInstances() {
		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.addModule( one );
		builder.addModule( two );

		ModuleSet moduleSet = builder.build();
		assertEquals( list( one, two ), moduleSet.getModules() );

		Map<String, AcrossModule> moduleMap = moduleSet.getModuleMap();
		assertNotNull( moduleMap );
		assertSame( one, moduleMap.get( one.getName() ) );
		assertSame( two, moduleMap.get( two.getName() ) );
	}

	@Test
	public void moduleResolving() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( one.getName(), true ) ).thenReturn( Optional.of( one ) );
		when( dependencyResolver.resolveModule( two.getName(), true ) ).thenReturn( Optional.of( two ) );

		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.addModule( one.getName() );
		builder.addModule( two.getName() );
		builder.setDependencyResolver( dependencyResolver );

		ModuleSet moduleSet = builder.build();
		assertEquals( list( one, two ), moduleSet.getModules() );

		Map<String, AcrossModule> moduleMap = moduleSet.getModuleMap();
		assertNotNull( moduleMap );
		assertSame( one, moduleMap.get( one.getName() ) );
		assertSame( two, moduleMap.get( two.getName() ) );
	}

	@Test
	public void modulesAreReplacedButKeptInAddingOrder() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( one.getName(), true ) )
				.thenReturn( Optional.of( new EmptyAcrossModule( "ModuleOne" ) ) );
		when( dependencyResolver.resolveModule( two.getName(), true ) )
				.thenReturn( Optional.of( new EmptyAcrossModule( "ModuleTwo" ) ) );

		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.addModule( two.getName() ); // resolves to empty
		builder.addModule( one );           // one
		builder.addModule( two );           // two
		builder.addModule( one.getName() ); // resolves to empty
		builder.setDependencyResolver( dependencyResolver );

		ModuleSet moduleSet = builder.build();
		assertEquals( list( two, one ), moduleSet.getModules() );

		assertTrue( moduleSet.getModuleMap().get( two.getName() ) instanceof ModuleTwo );
		assertTrue( moduleSet.getModuleMap().get( one.getName() ) instanceof EmptyAcrossModule );
	}

	@Test
	public void requiredDependency() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( one.getName(), true ) ).thenReturn( Optional.of( one ) );

		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.setDependencyResolver( dependencyResolver );
		builder.addModule( requiresOne );

		ModuleSet moduleSet = builder.build();
		assertEquals( list( requiresOne, one ), moduleSet.getModules() );

		assertTrue( moduleSet.getOptionalDependencies( one ).isEmpty() );
		assertTrue( moduleSet.getRequiredDependencies( one ).isEmpty() );
		assertEquals( Collections.singleton( "ModuleOne" ), moduleSet.getRequiredDependencies( requiresOne ) );
		assertTrue( moduleSet.getOptionalDependencies( requiresOne ).isEmpty() );
	}

	@Test
	public void optionalDependency() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( one.getName(), false ) ).thenReturn( Optional.of( one ) );
		when( dependencyResolver.resolveModule( two.getName(), true ) ).thenReturn( Optional.of( two ) );

		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.setDependencyResolver( dependencyResolver );
		builder.addModule( requiresTwoOptionalOne );

		ModuleSet moduleSet = builder.build();
		assertEquals( list( requiresTwoOptionalOne, two, one ), moduleSet.getModules() );

		assertTrue( moduleSet.getOptionalDependencies( one ).isEmpty() );
		assertTrue( moduleSet.getRequiredDependencies( one ).isEmpty() );
		assertTrue( moduleSet.getOptionalDependencies( two ).isEmpty() );
		assertTrue( moduleSet.getRequiredDependencies( two ).isEmpty() );
		assertEquals(
				Collections.singleton( "ModuleTwo" ), moduleSet.getRequiredDependencies( requiresTwoOptionalOne )
		);
		assertEquals(
				Collections.singleton( "ModuleOne" ), moduleSet.getOptionalDependencies( requiresTwoOptionalOne )
		);
	}

	@Test
	public void runtimeDependency() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( one.getName(), true ) ).thenReturn( Optional.of( one ) );
		when( dependencyResolver.resolveModule( two.getName(), true ) ).thenReturn( Optional.empty() );

		ModuleThree requiresOneTwo = new ModuleThree();
		requiresOneTwo.addRuntimeDependency( two.getName() );

		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.setDependencyResolver( dependencyResolver );
		builder.addModule( requiresOneTwo );

		ModuleSet moduleSet = builder.build();
		assertEquals( list( requiresOneTwo, one ), moduleSet.getModules() );

		assertTrue( moduleSet.getOptionalDependencies( one ).isEmpty() );
		assertTrue( moduleSet.getRequiredDependencies( one ).isEmpty() );
		assertEquals( Arrays.asList( "ModuleOne", "ModuleTwo" ),
		              new ArrayList<>( moduleSet.getRequiredDependencies( requiresOneTwo ) ) );
		assertTrue( moduleSet.getOptionalDependencies( requiresOne ).isEmpty() );
	}

	@Test
	public void moduleRolesAreRegistered() {
		ModuleSetBuilder builder = new ModuleSetBuilder();
		builder.addModule( one );
		builder.addModule( two );
		builder.addModule( requiresOne );
		builder.addModule( requiresTwoOptionalOne );

		ModuleSet moduleSet = builder.build();
		assertEquals( AcrossModuleRole.APPLICATION, moduleSet.getModuleRole( one ) );
		assertEquals( AcrossModuleRole.APPLICATION, moduleSet.getModuleRole( two ) );
		assertEquals( AcrossModuleRole.POSTPROCESSOR, moduleSet.getModuleRole( requiresOne ) );
		assertEquals( AcrossModuleRole.INFRASTRUCTURE, moduleSet.getModuleRole( requiresTwoOptionalOne ) );
	}

	private Collection<AcrossModule> list( AcrossModule... modules ) {
		return Arrays.asList( modules );
	}

	public static class ModuleOne extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleOne";
		}

		@Override
		public String getDescription() {
			return null;
		}
	}

	public static class ModuleTwo extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleTwo";
		}
	}

	@AcrossRole(AcrossModuleRole.POSTPROCESSOR)
	@AcrossDepends(required = "ModuleOne")
	public static class ModuleThree extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleThree";
		}
	}

	@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
	@AcrossDepends(required = "ModuleTwo", optional = "ModuleOne")
	public static class ModuleFour extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleFour";
		}
	}
}
