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
package test.scan;

import com.foreach.across.core.context.ModuleConfigurationSet;
import com.foreach.across.core.context.module.ModuleConfigurationExtension;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 */
class TestModuleConfigurationSet
{
	private ModuleConfigurationSet set = new ModuleConfigurationSet();

	@Test
	void nothingRegistered() {
		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertEquals( 0, set.getConfigurations( "moduleTwo" ).length );
	}

	@Test
	void registerToAll() {
		set.register( deferred( One.class ) );

		assertThat( set.getConfigurations( "moduleOne" ) ).containsExactly( deferred( One.class ) );
		assertThat( set.getConfigurations( "moduleTwo" ) ).containsExactly( deferred( One.class ) );
	}

	@Test
	void includesNoImpactIfAlreadyRegisteredToAll() {
		set.register( deferred( One.class ) );
		set.register( deferred( One.class ), "moduleTwo" );

		assertThat( set.getConfigurations( "moduleOne" ) ).containsExactly( deferred( One.class ) );
		assertThat( set.getConfigurations( "moduleTwo" ) ).containsExactly( deferred( One.class ) );
	}

	@Test
	void specificIncludes() {
		set.register( deferred( One.class ), "moduleTwo" );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertThat( set.getConfigurations( "moduleTwo" ) ).containsExactly( deferred( One.class ) );
	}

	@Test
	void aliasing() {
		set.register( deferred( One.class ), "moduleTwo" );
		set.register( deferred( One.class ), "aliasOne" );
		set.register( deferred( Three.class ), "aliasOne" );
		set.register( deferred( Two.class ), "aliasTwo" );

		assertThat( set.getConfigurations( "moduleOne", "aliasOne", "aliasTwo" ) )
				.containsExactly( deferred( One.class ), deferred( Three.class ), deferred( Two.class ) );
	}

	@Test
	void deferredAndNonDeferred() {
		set.register( deferred( One.class ), "moduleTwo" );
		set.register( nonDeferred( Two.class ), "moduleTwo" );
		set.register( nonDeferred( One.class ), "moduleTwo" );

		assertThat( set.getConfigurations( "moduleTwo" ) )
				.containsExactly( deferred( One.class ), nonDeferred( Two.class ), nonDeferred( One.class ) );
	}

	@Test
	void specificExcludes() {
		set.register( deferred( One.class ) );
		set.exclude( One.class, "moduleOne" );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertThat( set.getConfigurations( "moduleTwo" ) ).containsExactly( deferred( One.class ) );
		assertArrayEquals( new String[] { One.class.getName() }, set.getExcludedConfigurations( "moduleOne" ) );
		assertEquals( 0, set.getExcludedConfigurations( "moduleTwo" ).length );
	}

	@Test
	void excludeTakesPrecedence() {
		set.register( deferred( One.class ), "moduleOne", "moduleTwo" );
		set.exclude( One.class, "moduleOne" );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertThat( set.getConfigurations( "moduleTwo" ) ).containsExactly( deferred( One.class ) );
	}

	@Test
	void removeRegistrations() {
		set.register( deferred( One.class ), "moduleOne", "moduleTwo" );
		set.exclude( One.class, "moduleTwo" );
		set.remove( One.class );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertEquals( 0, set.getConfigurations( "moduleTwo" ).length );
		assertEquals( 0, set.getExcludedConfigurations( "moduleOne" ).length );
		assertEquals( 0, set.getExcludedConfigurations( "moduleTwo" ).length );
	}

	@Test
	void registrationsAreKeptInOrderEvenAfterUpdates() {
		set.register( deferred( One.class ) );
		set.register( deferred( Two.class ), "moduleTwo" );
		set.register( deferred( Three.class ), "moduleOne", "moduleTwo" );
		set.register( deferred( One.class ), "moduleOne" );

		assertThat( set.getConfigurations( "moduleOne" ) ).containsExactly( deferred( One.class ), deferred( Three.class ) );
		assertThat( set.getConfigurations( "moduleTwo" ) ).containsExactly( deferred( One.class ), deferred( Two.class ), deferred( Three.class ) );
	}

	private ModuleConfigurationExtension deferred( Class c ) {
		return ModuleConfigurationExtension.of( c.getTypeName(), true, false );
	}

	private ModuleConfigurationExtension nonDeferred( Class c ) {
		return ModuleConfigurationExtension.of( c.getTypeName(), false, false );
	}

	private static class One
	{

	}

	private static class Two
	{

	}

	private static class Three
	{

	}
}
