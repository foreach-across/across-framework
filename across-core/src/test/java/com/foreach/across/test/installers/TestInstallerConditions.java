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

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestInstallerConditions.Config.class)
@TestPropertySource(properties = { "spring.profiles.active=dev", "active.value=true" })
public class TestInstallerConditions
{
	private JdbcTemplate core;

	@Autowired
	private void setAcrossDataSource( EmbeddedDatabase acrossDataSource ) {
		core = new JdbcTemplate( acrossDataSource );
	}

	@Test
	public void registeredInstallers() {
		assertInstalled( NoConditionInstaller.class );
		assertNotInstalled( BadProfileInstaller.class );
		assertInstalled( ActiveProfileInstaller.class );
		assertNotInstalled( ModuleMissingInstaller.class );
		assertInstalled( ModulePresentInstaller.class );
		assertNotInstalled( InvalidConditionInstaller.class );
		assertInstalled( ValidConditionInstaller.class );
		assertInstalled( OtherValidConditionInstaller.class );
	}

	private void assertInstalled( Class<?> installerClass ) {
		assertEquals(
				installerClass.getSimpleName() + " was not installed",
				Integer.valueOf( 1 ),
				core.queryForObject(
						"SELECT count(*) FROM acrossmodules WHERE installer_id = '" + installerClass.getName() + "'",
						Integer.class
				)
		);
	}

	private void assertNotInstalled( Class<?> installerClass ) {
		assertEquals(
				installerClass.getSimpleName() + " was installed but was not supposed to be",
				Integer.valueOf( 0 ),
				core.queryForObject(
						"SELECT count(*) FROM acrossmodules WHERE installer_id = '" + installerClass.getName() + "'",
						Integer.class
				)
		);
	}

	@Configuration
	@EnableAcrossContext
	static class Config
	{
		@Bean
		public InstallerConditionsModule installerConditionsModule() {
			return new InstallerConditionsModule();
		}

		@Bean
		public AcrossModule dummyModule() {
			return new EmptyAcrossModule( "DummyModule" );
		}

		@Bean
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "core" )
					.build();
		}
	}

	static class InstallerConditionsModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "InstallerConditionsModule";
		}

		@Override
		public String getDescription() {
			return "Has multiple installers with different conditions.";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] {
					NoConditionInstaller.class, BadProfileInstaller.class, ActiveProfileInstaller.class,
					ModuleMissingInstaller.class, ModulePresentInstaller.class, InvalidConditionInstaller.class,
					ValidConditionInstaller.class, OtherValidConditionInstaller.class
			};
		}
	}

	static abstract class BaseInstaller
	{
		@InstallerMethod
		public void install() {
		}
	}

	@Installer(description = "Should be registered, no additional condition.")
	static class NoConditionInstaller extends BaseInstaller
	{
	}

	@Profile("not-active")
	@Installer(description = "Should not be registered, profile not active.")
	static class BadProfileInstaller extends BaseInstaller
	{
	}

	@Profile("dev")
	@Installer(description = "Should be registered, profile is active.")
	static class ActiveProfileInstaller extends BaseInstaller
	{
	}

	@AcrossDepends(required = "ModuleNotPresent")
	@Installer(description = "Should not be registered as module is not configured.")
	static class ModuleMissingInstaller extends BaseInstaller
	{
	}

	@AcrossDepends(required = "DummyModule")
	@Installer(description = "Should be registered as module is configured.")
	static class ModulePresentInstaller extends BaseInstaller
	{
	}

	@AcrossCondition("${illegal.value:false}")
	@Installer(description = "Should not be registered as condition evaluates to false.")
	static class InvalidConditionInstaller extends BaseInstaller
	{
	}

	@AcrossCondition("${active.value:false}")
	@Installer(description = "Should be registered as condition evaluates to true.")
	static class ValidConditionInstaller extends BaseInstaller
	{
	}

	@AcrossCondition("currentModule.name == 'InstallerConditionsModule'")
	@Installer(description = "Should be registered as condition evaluates to true.")
	static class OtherValidConditionInstaller extends BaseInstaller
	{
	}
}
