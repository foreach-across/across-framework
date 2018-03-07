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

import com.foreach.across.AcrossContextLoader;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.SingletonBeanConfigurer;
import com.foreach.across.core.installers.InstallerPhase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestInstallerConditionals.Config.class, loader = AcrossContextLoader.class)
@ActiveProfiles("dev")
@TestPropertySource(properties = "active.value=true")
public class TestInstallerConditionals
{
	private static Set<Class<?>> createdInstallerBeans = new HashSet<>();

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
		assertInstalled( ExpressionInstaller.class );
		assertNotInstalled( InvalidBeanConditionInstaller.class );
		assertInstalled( ValidBeanConditionInstaller.class );
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
		assertTrue( createdInstallerBeans.contains( installerClass ) );
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
		assertFalse( createdInstallerBeans.contains( installerClass ) );
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
					ValidConditionInstaller.class, OtherValidConditionInstaller.class, ExpressionInstaller.class,
					InvalidBeanConditionInstaller.class, ValidBeanConditionInstaller.class
			};
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> applicationContextConfigurers ) {
			applicationContextConfigurers.add( new SingletonBeanConfigurer( "myBean", new HashMap<>() ) );
		}
	}

	static abstract class BaseInstaller
	{
		@PostConstruct
		public void registerBeanCreation() {
			createdInstallerBeans.add( getClass() );
		}

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
		public BadProfileInstaller() {
		}
	}

	@Profile("dev")
	@Installer(description = "Should be registered, profile is active.")
	static class ActiveProfileInstaller extends BaseInstaller
	{
	}

	@ConditionalOnAcrossModule("ModuleNotPresent")
	@Installer(description = "Should not be registered as module is not configured.")
	static class ModuleMissingInstaller extends BaseInstaller
	{
	}

	@ConditionalOnAcrossModule("DummyModule")
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

	@ConditionalOnExpression("@'across.currentModule'.name == 'InstallerConditionsModule'")
	@Installer(description = "Should be registered as condition evaluates to true.")
	static class ExpressionInstaller extends BaseInstaller
	{
	}

	@ConditionalOnBean(name = "myBean")
	@Installer(description = "Registered as bean is available once module is bootstrapped.", phase = InstallerPhase.AfterModuleBootstrap)
	static class ValidBeanConditionInstaller extends BaseInstaller
	{
	}

	@ConditionalOnBean(name = "myBean")
	@Installer(description = "Not registered as bean is not available before context.")
	static class InvalidBeanConditionInstaller extends BaseInstaller
	{
	}
}
