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
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.core.installers.InstallerActionResolver;
import com.foreach.across.core.installers.InstallerMetaData;
import com.foreach.across.core.installers.InstallerSettings;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
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
	public void defaultActionOfConstructedSettingsIsExecute() {
		assertEquals( InstallerAction.EXECUTE, settings.getDefaultAction() );
	}

	@Test
	public void defaultActionAppliesIfNothingSpecificIsSet() {
		for ( InstallerAction defaultAction : InstallerAction.values() ) {
			settings.setDefaultAction( defaultAction );

			InstallerAction action = settings.shouldRun( "module", installer( "test", "group" ) );
			assertEquals( defaultAction, action );

			action = settings.shouldRun(
					RandomStringUtils.random( 10 ),
					installer( RandomStringUtils.random( 50 ), RandomStringUtils.random( 10 ) )
			);
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
			settings.setActionForInstallers( InstallerAction.EXECUTE, "one", "two" );
			settings.setActionForInstallers( InstallerAction.FORCE, InstallerOne.class );
			settings.setActionForInstallers( InstallerAction.REGISTER, InstallerTwo.class );
			settings.setActionForInstallers( InstallerAction.DISABLED, "three" );
			settings.setActionForInstallers( InstallerAction.SKIP, "four", "five" );

			// Fetch actions for known installers
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( "one" ) ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( "two" ) ) );
			assertEquals( InstallerAction.FORCE,
			              settings.shouldRun( rnd(), installer( InstallerOne.class.getName() ) ) );

			assertEquals( InstallerAction.REGISTER,
			              settings.shouldRun( rnd(), installer( InstallerTwo.class.getName() ) ) );
			assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( "three" ) ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( rnd(), installer( "five" ) ) );

			//  Unknown installer should always return the default action
			assertEquals( defaultAction, settings.shouldRun( rnd(), installer( rnd() ) ) );
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
			settings.setActionForInstallerGroups( InstallerAction.REGISTER, "registerOne", "registerTwo" );
			settings.setActionForInstallerGroups( InstallerAction.DISABLED, "disabled" );

			// Set action for specific installer
			settings.setActionForInstallers( InstallerAction.FORCE, InstallerOne.class.getName() );

			// Fetch actions for known installers
			String name = InstallerOne.class.getName();

			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( rnd(), "executeOne" ) ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( rnd(), "executeTwo" ) ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( rnd(), installer( rnd(), "skipOne" ) ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( rnd(), installer( rnd(), "skipTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( rnd(), "force" ) ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( rnd(), installer( rnd(), "registerOne" ) ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( rnd(), installer( rnd(), "registerTwo" ) ) );
			assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "disabled" ) ) );

			// Installer rule should win
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "executeOne" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "executeTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "skipOne" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "skipTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "force" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "registerOne" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "registerTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "disabled" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, rnd() ) ) );

			// Unknown group should always return the default action
			assertEquals( defaultAction, settings.shouldRun( rnd(), installer( rnd(), rnd() ) ) );
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
			settings.setActionForInstallerGroups( InstallerAction.REGISTER, "registerOne", "registerTwo" );
			settings.setActionForInstallerGroups( InstallerAction.DISABLED, "disabled" );

			// Set action for specific installer
			settings.setActionForInstallers( InstallerAction.FORCE, InstallerOne.class.getName() );

			// Fetch actions for known installers
			String name = InstallerOne.class.getName();

			// In these cases the priority resolver should decide
			when( priorityResolver.resolve( anyString(), any( InstallerMetaData.class ) ) )
					.thenReturn( Optional.empty() );
			when( priorityResolver.resolve( anyString(), eq( installer( "one", "skipOne" ) ) ) )
					.thenReturn( Optional.of( InstallerAction.REGISTER ) );
			when( priorityResolver.resolve( anyString(), eq( installer( "two", "registerOne" ) ) ) )
					.thenReturn( Optional.of( InstallerAction.SKIP ) );
			when( priorityResolver.resolve( anyString(), eq( installer( "three", "disabled" ) ) ) )
					.thenReturn( Optional.of( InstallerAction.EXECUTE ) );

			// Priority resolver action
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( rnd(), installer( "one", "skipOne" ) ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( rnd(), installer( "two", "registerOne" ) ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( "three", "disabled" ) ) );

			// Group actions
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( rnd(), "executeOne" ) ) );
			assertEquals( InstallerAction.EXECUTE, settings.shouldRun( rnd(), installer( rnd(), "executeTwo" ) ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( rnd(), installer( rnd(), "skipOne" ) ) );
			assertEquals( InstallerAction.SKIP, settings.shouldRun( rnd(), installer( rnd(), "skipTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( rnd(), "force" ) ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( rnd(), installer( rnd(), "registerOne" ) ) );
			assertEquals( InstallerAction.REGISTER, settings.shouldRun( rnd(), installer( rnd(), "registerTwo" ) ) );
			assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "disabled" ) ) );

			// Installer rule should win
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "executeOne" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "executeTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "skipOne" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "skipTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "force" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "registerOne" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "registerTwo" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, "disabled" ) ) );
			assertEquals( InstallerAction.FORCE, settings.shouldRun( rnd(), installer( name, rnd() ) ) );

			// Unknown group should always return the default action
			assertEquals( defaultAction, settings.shouldRun( rnd(), installer( rnd(), rnd() ) ) );
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
		settings.setActionForInstallerGroups( InstallerAction.REGISTER, "registerOne", "registerTwo" );
		settings.setActionForInstallerGroups( InstallerAction.DISABLED, "disabled" );

		// Set action for specific installer
		settings.setActionForInstallers( InstallerAction.FORCE, InstallerOne.class.getName() );

		// Fetch actions for known installers
		String name = InstallerOne.class.getName();

		// In these cases the priority resolver should decide
		when( priorityResolver.resolve( anyString(), any( InstallerMetaData.class ) ) )
				.thenReturn( Optional.empty() );
		when( priorityResolver.resolve( anyString(), eq( installer( "one", "skipOne" ) ) ) )
				.thenReturn( Optional.of( InstallerAction.REGISTER ) );
		when( priorityResolver.resolve( anyString(), eq( installer( "two", "registerOne" ) ) ) )
				.thenReturn( Optional.of( InstallerAction.SKIP ) );
		when( priorityResolver.resolve( anyString(), eq( installer( "three", "disabled" ) ) ) )
				.thenReturn( Optional.of( InstallerAction.EXECUTE ) );

		// Priority resolver action
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( "one", "skipOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( "two", "registerOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( "three", "disabled" ) ) );

		// Group actions
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "executeOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "executeTwo" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "skipOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "skipTwo" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "force" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "registerOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "registerTwo" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), "disabled" ) ) );

		// Installer rule should win
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "executeOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "executeTwo" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "skipOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "skipTwo" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "force" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "registerOne" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "registerTwo" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, "disabled" ) ) );
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( name, rnd() ) ) );

		// Unknown group should always return the default action
		assertEquals( InstallerAction.DISABLED, settings.shouldRun( rnd(), installer( rnd(), rnd() ) ) );
	}

	private String rnd() {
		return RandomStringUtils.random( 10 );
	}

	private InstallerMetaData installer( String installerName ) {
		return installer( installerName, "" );
	}

	private InstallerMetaData installer( String installerName, String group ) {
		ConfigurableMetaData metaData = new ConfigurableMetaData();
		metaData.setName( installerName );
		metaData.setGroup( group );

		return metaData;
	}

	protected static class ConfigurableMetaData extends InstallerMetaData
	{
		@Override
		protected void setName( String name ) {
			super.setName( name );
		}

		@Override
		protected void setGroup( String group ) {
			super.setGroup( group );
		}
	}

	@Installer(description = "one")
	private static class InstallerOne
	{
		@InstallerMethod
		public void install() {
		}
	}

	@Installer(description = "does something")
	private static class InstallerTwo
	{
		@InstallerMethod
		public void install() {
		}
	}
}
