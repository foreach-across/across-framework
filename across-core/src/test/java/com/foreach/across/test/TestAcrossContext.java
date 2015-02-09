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

package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.modules.exposing.ExposingModule;
import com.foreach.across.test.modules.installer.InstallerModule;
import com.foreach.across.test.modules.module1.TestModule1;
import com.foreach.across.test.modules.module2.TestModule2;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestAcrossContext
{
	@Test
	public void getModuleByName() {
		TestModule1 module = new TestModule1();
		ExposingModule other = new ExposingModule( "my module" );

		AcrossContext context = new AcrossContext();
		context.addModule( module );
		context.addModule( other );

		assertNull( context.getModule( "not present" ) );
		assertSame( module, context.getModule( module.getName() ) );
		assertSame( other, context.getModule( "my module" ) );
		assertSame( other, context.getModule( "com.foreach.across.test.modules.exposing.ExposingModule" ) );
	}

	@Test
	public void moduleWithTheSameNameIsNotAllowedWhenBootstrapping() {
		AcrossContext context = new AcrossContext();
		context.setInstallerAction( InstallerAction.DISABLED );

		TestModule1 module = new TestModule1();
		context.addModule( module );
		context.addModule( new TestModule2() );
		context.addModule( new ExposingModule( module.getName() ) );

		boolean failed = false;

		try {
			context.bootstrap();
		}
		catch ( RuntimeException re ) {
			failed = true;
		}

		assertTrue( "Bootstrapping modules with the same name should not be possible", failed );
	}

	@Test
	public void dataSourceIsNotRequiredIfNoInstallers() {
		AcrossContext context = new AcrossContext();
		context.setInstallerAction( InstallerAction.EXECUTE );
		context.addModule( new TestModule1() );

		context.bootstrap();
	}

	@Test
	public void dataSourceIsNotRequiredIfInstallersWontRun() {
		AcrossContext context = new AcrossContext();
		// Default installer action is disabled
		context.addModule( new InstallerModule() );

		context.bootstrap();
	}

	@Test
	public void dataSourceIsRequiredIfInstallersWantToRun() {
		AcrossContext context = new AcrossContext();
		context.setInstallerAction( InstallerAction.EXECUTE );
		context.addModule( new InstallerModule() );

		boolean failed = false;

		try {
			context.bootstrap();
		}
		catch ( RuntimeException re ) {
			failed = true;
		}

		assertTrue( "A datasource should be required if installers want to run.", failed );
	}

	@Test
	public void dataSourceIsNotRequiredIfInstallerDataSourceIsAvailable() {
		AcrossContext context = new AcrossContext();
		context.setInstallerAction( InstallerAction.EXECUTE );
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:acrossTest" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		context.setInstallerDataSource( dataSource );
		context.addModule( new InstallerModule() );

		context.bootstrap();
	}

	@Test
	public void unableToAddModuleAfterBootstrap() {
		AcrossContext context = new AcrossContext();
		context.bootstrap();
	}

	@Test
	public void sameModuleIsNotAllowed() {
		AcrossContext context = new AcrossContext();

		TestModule1 duplicate = new TestModule1();

		context.addModule( duplicate );
		context.addModule( new TestModule2() );

		boolean failed = false;

		try {
			context.addModule( duplicate );
		}
		catch ( RuntimeException re ) {
			failed = true;
		}

		assertTrue( "Adding same module instance should not be allowed", failed );
	}

	@Test
	public void sameModuleCanOnlyBeInOneAcrossContext() {
		TestModule1 module = new TestModule1();

		AcrossContext contextOne = new AcrossContext();
		contextOne.addModule( module );

		AcrossContext contextTwo = new AcrossContext();

		boolean failed = false;

		try {
			contextTwo.addModule( module );
		}
		catch ( RuntimeException re ) {
			failed = true;
		}

		assertTrue( "Adding same module to another Across context should not be allowed", failed );
	}
}
