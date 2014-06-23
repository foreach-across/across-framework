package com.foreach.across.test.installers;

import com.foreach.across.core.installers.InstallerSettings;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestInstallerSettings
{
	private InstallerSettings settings;

	@Before
	public void createSettings() {
		settings = new InstallerSettings();
	}

	@Test
	public void defaultActionAppliesIfNothingSpecificIsSet() {
		for ( InstallerSettings.Action defaultAction : InstallerSettings.Action.values() ) {
			settings.setDefaultAction( defaultAction );

			InstallerSettings.Action action = settings.shouldRun( "group", "test" );
			assertEquals( defaultAction, action );

			action = settings.shouldRun( RandomStringUtils.random( 10 ), RandomStringUtils.random( 50 ) );
			assertEquals( defaultAction, action );
		}
	}

	@Test
	public void specificInstallerActionIsMostImportant() {
		for ( InstallerSettings.Action defaultAction : new InstallerSettings.Action[] {
				InstallerSettings.Action.EXECUTE,
				InstallerSettings.Action.SKIP,
				InstallerSettings.Action.FORCE,
				InstallerSettings.Action.REGISTER } ) {
			settings.setDefaultAction( defaultAction );

			// Set actions for known installers
			settings.setActionForInstallers( InstallerSettings.Action.EXECUTE, "java.util.Date",
			                                 "java.lang.Throwable" );
			settings.setActionForInstallers( InstallerSettings.Action.SKIP, new Integer( 5 ), new Long( 6 ) );
			settings.setActionForInstallers( InstallerSettings.Action.FORCE, BigDecimal.class, BigInteger.class );
			settings.setActionForInstallers( InstallerSettings.Action.REGISTER, Arrays.asList( String.class,
			                                                                                   Short.class ) );
			settings.setActionForInstallers( InstallerSettings.Action.DISABLED, "java.lang.Exception" );

			// Fetch actions for known installers
			assertEquals( InstallerSettings.Action.EXECUTE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                    new Date() ) );
			assertEquals( InstallerSettings.Action.EXECUTE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                    new Throwable() ) );
			assertEquals( InstallerSettings.Action.SKIP, settings.shouldRun( RandomStringUtils.random( 10 ), 7 ) );
			assertEquals( InstallerSettings.Action.SKIP, settings.shouldRun( RandomStringUtils.random( 10 ), 60L ) );
			assertEquals( InstallerSettings.Action.FORCE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                  new BigDecimal( 0 ) ) );
			assertEquals( InstallerSettings.Action.FORCE, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                  new BigInteger( "0" ) ) );
			assertEquals( InstallerSettings.Action.REGISTER, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                     "test" ) );
			assertEquals( InstallerSettings.Action.REGISTER, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                     new Short( "1" ) ) );
			assertEquals( InstallerSettings.Action.DISABLED, settings.shouldRun( RandomStringUtils.random( 10 ),
			                                                                     new Exception() ) );

			//  Unknown installer should always return the default action
			assertEquals( defaultAction, settings.shouldRun( RandomStringUtils.random( 10 ), new Object() ) );
		}
	}

	@Ignore
	@Test
	public void specificGroupActionIfNoInstallerAction() {
		fail( "not done" );
	}

	@Ignore
	@Test
	public void customInstallerFilterTrumpsInstallerSetting() {
		fail( "not done" );
	}

	@Ignore
	@Test
	public void defaultDisabledTrumpsAllOtherSettings() {
		fail( "not done" );
	}

}
