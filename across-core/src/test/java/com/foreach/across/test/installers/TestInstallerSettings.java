package com.foreach.across.test.installers;

import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.core.installers.InstallerActionResolver;
import com.foreach.across.core.installers.InstallerSettings;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInstallerSettings
{
	private InstallerSettings settings;

	@Before
	public void createSettings() {
		settings = new InstallerSettings();
	}

	@Test
	public void defaultActionAppliesIfNothingSpecificIsSet() {
		for ( InstallerAction defaultAction : InstallerAction.values() ) {
			settings.setDefaultAction( defaultAction );

			InstallerAction action = settings.shouldRun( "group", "test" );
			assertEquals( defaultAction, action );

			action = settings.shouldRun( RandomStringUtils.random( 10 ), RandomStringUtils.random( 50 ) );
			assertEquals( defaultAction, action );
		}
	}

	@Test
	public void specificInstallerActionIsMostImportant() {
		for ( InstallerAction defaultAction : new InstallerAction[] {
				InstallerAction.EXECUTE,
				InstallerAction.SKIP,
				InstallerAction.FORCE,
				InstallerAction.REGISTER } ) {
			settings.setDefaultAction( defaultAction );

			// Set actions for known installers
			settings.setActionForInstallers( InstallerAction.EXECUTE, "java.util.Date",
			                                 "java.lang.Throwable" );
			settings.setActionForInstallers( InstallerAction.SKIP, new Integer( 5 ), new Long( 6 ) );
			settings.setActionForInstallers( InstallerAction.FORCE, BigDecimal.class, BigInteger.class );
			settings.setActionForInstallers( InstallerAction.REGISTER, Arrays.asList( String.class,
			                                                                          Short.class ) );
			settings.setActionForInstallers( InstallerAction.DISABLED, "java.lang.Exception" );

			// Fetch actions for known installers
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                           new Date() ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                           new Throwable() ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( RandomStringUtils.random( 10 ), 7 ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( RandomStringUtils.random( 10 ), 60L ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                         new BigDecimal( 0 ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                         new BigInteger( "0" ) ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                            "test" ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                            new Short( "1" ) ) );
			assertEquals( InstallerAction.DISABLED, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                            new Exception() ) );

			//  Unknown installer should always return the default action
			assertEquals( defaultAction, settings.shouldRun( RandomStringUtils.random( 10 ), new Object() ) );
		}
	}

	@Test
	public void specificGroupActionIfNoInstallerAction() {
		for ( InstallerAction defaultAction : new InstallerAction[] {
				InstallerAction.EXECUTE,
				InstallerAction.SKIP,
				InstallerAction.FORCE,
				InstallerAction.REGISTER } ) {
			settings.setDefaultAction( defaultAction );

			// Set actions for groups
			settings.setActionForInstallerGroups( InstallerAction.EXECUTE, "executeOne", "executeTwo" );
			settings.setActionForInstallerGroups( InstallerAction.SKIP, "skipOne", "skipTwo" );
			settings.setActionForInstallerGroups( InstallerAction.FORCE, "force" );
			settings.setActionForInstallerGroups( InstallerAction.REGISTER, Arrays.asList( "registerOne",
			                                                                               "registerTwo" ) );
			settings.setActionForInstallerGroups( InstallerAction.DISABLED, "disabled" );

			// Set action for specific installer
			settings.setActionForInstallers( InstallerAction.FORCE, "java.lang.String" );

			// Fetch actions for known installers
			Object groupRule = new Object();
			String installerRule = "installerRule";

			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( "executeOne", groupRule ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( "executeTwo", groupRule ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( "skipOne", groupRule ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( "skipTwo", groupRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "force", groupRule ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( "registerOne", groupRule ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( "registerTwo", groupRule ) );
			assertEquals( InstallerAction.DISABLED, settings.shouldRun( "disabled", groupRule ) );

			// Installer rule should win
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "executeOne", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "executeTwo", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "skipOne", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "skipTwo", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "force", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "registerOne", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "registerTwo", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "disabled", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( RandomStringUtils.random( 4 ),
			                                                         installerRule ) );

			// Unknown group should always return the default action
			assertEquals( defaultAction, settings.shouldRun( RandomStringUtils.random( 4 ), groupRule ) );
		}
	}

	@Test
	public void customInstallerFilterTrumpsInstallerSetting() {
		for ( InstallerAction defaultAction : new InstallerAction[] {
				InstallerAction.EXECUTE,
				InstallerAction.SKIP,
				InstallerAction.FORCE,
				InstallerAction.REGISTER } ) {
			settings.setDefaultAction( defaultAction );

			InstallerActionResolver priorityResolver = mock( InstallerActionResolver.class );
			settings.setPriorityActionResolver( priorityResolver );

			// Set actions for groups
			settings.setActionForInstallerGroups( InstallerAction.EXECUTE, "executeOne", "executeTwo" );
			settings.setActionForInstallerGroups( InstallerAction.SKIP, "skipOne", "skipTwo" );
			settings.setActionForInstallerGroups( InstallerAction.FORCE, "force" );
			settings.setActionForInstallerGroups( InstallerAction.REGISTER, Arrays.asList( "registerOne",
			                                                                               "registerTwo" ) );
			settings.setActionForInstallerGroups( InstallerAction.DISABLED, "disabled" );

			// Set action for specific installer
			settings.setActionForInstallers( InstallerAction.FORCE, "java.lang.String" );

			// Fetch actions for known installers
			Object groupRule = new Object();
			String installerRule = "installerRule";

			// In these cases the priority resolver should decide
			when( priorityResolver.resolve( "skipOne", groupRule ) ).thenReturn( InstallerAction.REGISTER );
			when( priorityResolver.resolve( "registerOne", groupRule ) ).thenReturn( InstallerAction.SKIP );
			when( priorityResolver.resolve( "disabled", installerRule ) ).thenReturn( InstallerAction.EXECUTE );

			// Priority resolver action
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( "skipOne", groupRule ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( "registerOne", groupRule ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( "disabled", installerRule ) );

			// Group actions
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( "executeOne", groupRule ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( "executeTwo", groupRule ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( "skipTwo", groupRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "force", groupRule ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( "registerTwo", groupRule ) );
			assertEquals( InstallerAction.DISABLED, settings.shouldRun( "disabled", groupRule ) );

			// Installer rule should win
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "executeOne", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "executeTwo", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "skipOne", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "skipTwo", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "force", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "registerOne", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( "registerTwo", installerRule ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( RandomStringUtils.random( 4 ),
			                                                         installerRule ) );

			// Unknown group should always return the default action
			assertEquals( defaultAction, settings.shouldRun( RandomStringUtils.random( 4 ), groupRule ) );
		}
	}

	@Test
	public void defaultDisabledTrumpsAllOtherSettings() {
		settings.setDefaultAction( InstallerAction.DISABLED );

		InstallerActionResolver priorityResolver = mock( InstallerActionResolver.class );
		settings.setPriorityActionResolver( priorityResolver );

		// Set actions for groups
		settings.setActionForInstallerGroups( InstallerAction.EXECUTE, "executeOne", "executeTwo" );
		settings.setActionForInstallerGroups( InstallerAction.SKIP, "skipOne", "skipTwo" );
		settings.setActionForInstallerGroups( InstallerAction.FORCE, "force" );
		settings.setActionForInstallerGroups( InstallerAction.REGISTER, Arrays.asList( "registerOne",
		                                                                               "registerTwo" ) );
		settings.setActionForInstallerGroups( InstallerAction.DISABLED, "disabled" );

		// Set action for specific installer
		settings.setActionForInstallers( InstallerAction.FORCE, "java.lang.String" );

		// Fetch actions for known installers
		Object groupRule = new Object();
		String installerRule = "installerRule";

		// In these cases the priority resolver should decide
		when( priorityResolver.resolve( "skipOne", groupRule ) ).thenReturn( InstallerAction.REGISTER );
		when( priorityResolver.resolve( "registerOne", groupRule ) ).thenReturn( InstallerAction.SKIP );
		when( priorityResolver.resolve( "disabled", installerRule ) ).thenReturn( InstallerAction.EXECUTE );

		// No matter it should always return DISABLED
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "skipOne", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "registerOne", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "disabled", installerRule ) );

		// Group actions
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "executeOne", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "executeTwo", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "skipTwo", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "force", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "registerTwo", groupRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "disabled", groupRule ) );

		// Installer rule should win
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "executeOne", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "executeTwo", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "skipOne", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "skipTwo", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "force", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "registerOne", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( "registerTwo", installerRule ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( RandomStringUtils.random( 4 ),
		                                                            installerRule ) );

		// Unknown group should always return the default action
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( RandomStringUtils.random( 4 ), groupRule ) );
	}

}
