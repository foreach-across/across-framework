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
package test.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.context.installers.InstallerSetBuilder;
import test.installers.examples.InstallerThree;
import test.installers.scan.installers.InstallerOne;
import test.installers.scan.installers.InstallerTwo;
import org.junit.Before;
import org.junit.Test;
import test.installers.examples.InstallerThree;
import test.installers.scan.installers.InstallerOne;
import test.installers.scan.installers.InstallerTwo;

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
	public void manualInstallersWithoutOrder() {
		ManualOne one = new ManualOne();

		builder.add( one, ManualTwo.class );
		builder.add( ManualThree.class );

		assertInstallers( one, ManualTwo.class, ManualThree.class );
	}

	@Test
	public void manualInstallersWithOrder() {
		InstallerOne installerOne = new InstallerOne();

		builder.add( installerOne );
		builder.add( InstallerThree.class, InstallerTwo.class );

		assertInstallers( InstallerTwo.class, installerOne, InstallerThree.class );
	}

	@Test
	public void mixedOrderingOnManualInstallers() {
		ManualOne one = new ManualOne();
		InstallerOne installerOne = new InstallerOne();

		builder.add( one );
		builder.add( installerOne );
		builder.add( InstallerThree.class, InstallerTwo.class );
		builder.add( ManualTwo.class );

		assertInstallers( one, ManualTwo.class,
		                  InstallerTwo.class, installerOne, InstallerThree.class );
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
		builder.scan( "test.installers.scan.installers",
		              "test.installers.examples" );

		assertInstallers( InstallerTwo.class, InstallerOne.class, InstallerThree.class );
	}

	@Test
	public void manualAndScannedCombined() {
		ManualOne one = new ManualOne();

		builder.add( ManualTwo.class );
		builder.scan( "test.installers.scan.installers" );
		builder.add( one );
		builder.scan( "test.installers.examples" );

		assertInstallers( ManualTwo.class, one, InstallerTwo.class, InstallerOne.class, InstallerThree.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateInstallerNameAddedManually() {
		builder.scan( "test.installers.examples" );
		builder.add( ManualThree.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateInstallerNameScanned() {
		builder.add( ManualThree.class );
		builder.scan( "test.installers.examples" );
	}

	@Test
	public void addingSameInstallerClassTwiceIsAllowed() {
		builder.scan( "test.installers.examples" );
		builder.add( InstallerThree.class );

		assertInstallers( InstallerThree.class );
	}

	@Test
	public void compatibilityOfManualAndScannedInstallers() {
		InstallerOne installerOne = new InstallerOne();

		builder.add( installerOne );
		builder.scan( "test.installers.scan.installers" );

		assertInstallers( InstallerTwo.class, installerOne );
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
