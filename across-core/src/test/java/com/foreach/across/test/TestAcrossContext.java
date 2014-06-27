package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.modules.exposing.ExposingModule;
import com.foreach.across.test.modules.module1.TestModule1;
import com.foreach.across.test.modules.module2.TestModule2;
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
	public void dataSourceIsRequiredWhenInstallersCanRun() {
		AcrossContext context = new AcrossContext();
		context.setInstallerAction( InstallerAction.EXECUTE );

		boolean failed = false;

		try {
			context.bootstrap();
		}
		catch ( RuntimeException re ) {
			failed = true;
		}

		assertTrue( "Bootstrapping with installers without datasource should not be possible", failed );
	}

	@Test
	public void dataSourceIsNotRequiredIfNoInstallers() {
		AcrossContext context = new AcrossContext();
		context.addModule( new TestModule1() );

		context.bootstrap();
	}

	@Test
	public void dataSourceIsNotRequiredIfInstallersDontWantToRun() {

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
