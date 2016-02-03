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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.installers.examples.InstallerThree;
import com.foreach.across.test.installers.scan.InstallerScanModule;
import com.foreach.across.test.installers.scan.installers.InstallerOne;
import com.foreach.across.test.installers.scan.installers.InstallerTwo;
import com.foreach.across.test.modules.installer.installers.TestInstaller;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestModuleInstallers
{
	@Before
	public void after() {
		TestInstaller.reset();
	}

	@Test
	public void scannedInstallers() {
		execute( new InstallerScanModule( false ) );
		assertInstalled( InstallerTwo.class, InstallerOne.class );
	}

	@Test
	public void manualAndScannedInstallers() {
		execute( new InstallerScanModule( true ) );
		assertInstalled( InstallerTwo.class, InstallerOne.class, InstallerThree.class );
	}

	@Test
	public void defaultInstallerContext() {
		execute( new InstallerScanModule( true ) );
		assertEquals( "fromInstallerContext", TestInstaller.WIRED_VALUES.get( InstallerOne.class ) );
		assertEquals( "fromInstallerContext", TestInstaller.WIRED_VALUES.get( InstallerTwo.class ) );
		assertEquals( "fromInstallerContext", TestInstaller.WIRED_VALUES.get( InstallerThree.class ) );
	}

	private void assertInstalled( Class<?>... installers ) {
		assertArrayEquals( installers, TestInstaller.executed() );
	}

	private void execute( InstallerScanModule module ) {
		AcrossContext ctx = new AcrossContext();
		ctx.setDataSource( new EmbeddedDatabaseBuilder().setType( EmbeddedDatabaseType.HSQL ).build() );
		ctx.setInstallerAction( InstallerAction.EXECUTE );
		ctx.addModule( module );
		ctx.bootstrap();
		ctx.shutdown();
	}
}
