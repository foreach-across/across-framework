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
package com.foreach.across.test.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.context.installers.InstallerSetBuilder;
import com.foreach.across.test.installers.examples.InstallerThree;
import com.foreach.across.test.installers.scan.installers.InstallerOne;
import com.foreach.across.test.installers.scan.installers.InstallerTwo;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Arne Vandamme
 */
public class TestInstallerSetBuilder
{
	private InstallerSetBuilder builder;

	@Before
	public void before() {
		builder = new InstallerSetBuilder();
	}

	@Test
	public void noInstallers() {
		assertInstallers();
	}

	@Test
	public void manualInstallers() {
		ManualOne one = new ManualOne();

		builder.add( one, ManualTwo.class );
		builder.add( ManualThree.class );

		assertInstallers( one, ManualTwo.class, ManualThree.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullPassed() {
		builder.add( ManualOne.class, null );
	}

	@Test(expected = IllegalArgumentException.class)
	public void beansWithoutInstallerAnnotation() {
		builder.add( "test" );
	}

	@Test(expected = IllegalArgumentException.class)
	public void classesWithoutInstallerAnnotation() {
		builder.add( Integer.class );
	}

	@Test
	public void scannedInstallers() {
		builder.scan( "com.foreach.across.test.installers.scan.installers",
		              "com.foreach.across.test.installers.examples" );

		Set<Object> installers = new HashSet<>();
		Collections.addAll( installers, builder.build() );

		assertThat(
				installers,
				is( new HashSet<>( Arrays.asList( InstallerOne.class, InstallerTwo.class, InstallerThree.class ) ) )
		);
	}

	@Test
	public void manualAndScannedCombined() {
		ManualOne one = new ManualOne();

		builder.add( ManualTwo.class );
		builder.scan( "com.foreach.across.test.installers.scan.installers" );
		builder.add( one );
		builder.scan( "com.foreach.across.test.installers.examples" );

		Set<Object> installers = new HashSet<>();
		Collections.addAll( installers, builder.build() );

		assertThat(
				installers,
				is( new HashSet<>( Arrays.asList(
						one, ManualTwo.class, InstallerOne.class, InstallerTwo.class, InstallerThree.class
				) ) )
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateInstallerNameAddedManually() {
		builder.scan( "com.foreach.across.test.installers.examples" );
		builder.add( ManualThree.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateInstallerNameScanned() {
		builder.add( ManualThree.class );
		builder.scan( "com.foreach.across.test.installers.examples" );
	}

	@Test
	public void addingSameInstallerClassTwiceIsAllowed() {
		builder.scan( "com.foreach.across.test.installers.examples" );
		builder.add( InstallerThree.class );

		assertInstallers( InstallerThree.class );
	}

	private void assertInstallers( Object... expected ) {
		assertArrayEquals( expected, builder.build() );
	}

	@Installer(description = "manualOne")
	protected static class ManualOne
	{
	}

	@Installer(description = "manualTwo")
	protected static class ManualTwo
	{
	}

	@Installer(description = "manualThree", name = "installerThree")
	protected static class ManualThree
	{
	}

}
