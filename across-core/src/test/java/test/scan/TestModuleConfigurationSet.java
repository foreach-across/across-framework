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
package test.scan;

import com.foreach.across.core.context.ModuleConfigurationSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestModuleConfigurationSet
{
	private ModuleConfigurationSet set;

	@Before
	public void setUp() throws Exception {
		set = new ModuleConfigurationSet();
	}

	@Test
	public void nothingRegistered() {
		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertEquals( 0, set.getConfigurations( "moduleTwo" ).length );
	}

	@Test
	public void registerToAll() {
		set.register( One.class );

		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleOne" ) );
		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleTwo" ) );
	}

	@Test
	public void includesNoImpactIfAlreadyRegisteredToAll() {
		set.register( One.class );
		set.register( One.class, "moduleTwo" );

		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleOne" ) );
		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleTwo" ) );
	}

	@Test
	public void specificIncludes() {
		set.register( One.class, "moduleTwo" );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleTwo" ) );
	}

	@Test
	public void aliasing() {
		set.register( One.class, "moduleTwo" );
		set.register( One.class, "aliasOne" );
		set.register( Three.class, "aliasOne" );
		set.register( Two.class, "aliasTwo" );

		String[] annotatedClasses = set.getConfigurations( "moduleOne", "aliasOne", "aliasTwo" );
		assertEquals( 3, annotatedClasses.length );
		assertArrayEquals( new String[] { One.class.getName(), Three.class.getName(), Two.class.getName() }, annotatedClasses );
	}

	@Test
	public void specificExcludes() {
		set.register( One.class );
		set.exclude( One.class, "moduleOne" );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleTwo" ) );
		assertArrayEquals( new String[] { One.class.getName() }, set.getExcludedConfigurations( "moduleOne" ) );
		assertEquals( 0, set.getExcludedConfigurations( "moduleTwo" ).length );
	}

	@Test
	public void excludeTakesPrecedence() {
		set.register( One.class, "moduleOne", "moduleTwo" );
		set.exclude( One.class, "moduleOne" );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertArrayEquals( new String[] { One.class.getName() }, set.getConfigurations( "moduleTwo" ) );
	}

	@Test
	public void removeRegistrations() {
		set.register( One.class, "moduleOne", "moduleTwo" );
		set.exclude( One.class, "moduleTwo" );
		set.remove( One.class );

		assertEquals( 0, set.getConfigurations( "moduleOne" ).length );
		assertEquals( 0, set.getConfigurations( "moduleTwo" ).length );
		assertEquals( 0, set.getExcludedConfigurations( "moduleOne" ).length );
		assertEquals( 0, set.getExcludedConfigurations( "moduleTwo" ).length );
	}

	@Test
	public void registrationsAreKeptInOrderEvenAfterUpdates() {
		set.register( One.class );
		set.register( Two.class, "moduleTwo" );
		set.register( Three.class, "moduleOne", "moduleTwo" );
		set.register( One.class, "moduleOne" );

		assertArrayEquals( new String[] { One.class.getName(), Three.class.getName() }, set.getConfigurations( "moduleOne" ) );
		assertArrayEquals( new String[] { One.class.getName(), Two.class.getName(), Three.class.getName() }, set.getConfigurations( "moduleTwo" ) );
	}

	static class One
	{
	}

	static class Two
	{
	}

	static class Three
	{

	}
}
