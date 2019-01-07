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
package com.foreach.across.test.datasource;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * @author Andy Somers
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestContextWithInstallerDataSource.Config.class)
public class TestContextWithInstallerDataSource
{
	@Autowired
	@Qualifier("mockDataSource")
	private DataSource mockDataSource;

	@Autowired
	private DataSource acrossInstallerDataSource;

	@Autowired
	private AcrossContext acrossContext;

	@Test
	public void dataSourcesShouldBeSet() {
		assertNotNull( acrossContext.getDataSource() );
		assertNotNull( acrossContext.getInstallerDataSource() );
		assertNotSame( acrossContext.getDataSource(), acrossContext.getInstallerDataSource() );
		assertSame( mockDataSource, acrossContext.getDataSource() );
		assertNotSame( mockDataSource, acrossInstallerDataSource );
	}

	@Test
	public void contextShouldBootstrapButInstallerShouldWireDefaultDataSource() {
		TestInstaller installer = ( (TestInstallerModule) acrossContext.getModule( "TestInstallerModule" ) )
				.getInstaller();
		assertTrue( installer.isExecuted() );
		assertSame( mockDataSource, installer.getPrimaryDataSource() );
	}

	@Test
	public void liquibaseInstallerShouldGetInstallerDataSource() {
		LiquibaseInstaller liquibaseInstaller = ( (TestInstallerModule) acrossContext.getModule(
				"TestInstallerModule" ) )
				.getLiquibaseInstaller();

		assertNotNull( liquibaseInstaller.getDataSource() );
		assertNotSame( mockDataSource, liquibaseInstaller.getDataSource() );
		assertSame( acrossInstallerDataSource, liquibaseInstaller.getDataSource() );
	}

	@Configuration
	@EnableAcrossContext
	static class Config implements AcrossContextConfigurer
	{
		@Bean(name = { "acrossDataSource", "mockDataSource" })
		public DataSource acrossDataSource() {
			return DataSourceBuilder.create().driverClassName( "org.hsqldb.jdbc.JDBCDriver" ).type( HikariDataSource.class )
			                        .url( "jdbc:hsqldb:mem:acrossTest" ).username( "sa" ).build();
		}

		@Bean
		public DataSource acrossInstallerDataSource() {
			return DataSourceBuilder.create().driverClassName( "org.hsqldb.jdbc.JDBCDriver" ).type( HikariDataSource.class )
			                        .url( "jdbc:hsqldb:mem:acrossTest" ).username( "sa" ).build();
		}

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new TestInstallerModule() );
		}
	}

	static class TestInstallerModule extends AcrossModule
	{
		private TestInstaller installer = new TestInstaller();
		private LiquibaseInstaller liquibaseInstaller = new LiquibaseInstaller();

		public TestInstaller getInstaller() {
			return installer;
		}

		public LiquibaseInstaller getLiquibaseInstaller() {
			return liquibaseInstaller;
		}

		@Override
		public String getName() {
			return "TestInstallerModule";
		}

		@Override
		public String getDescription() {
			return "Checks that default datasource is non-installer datasource.";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { installer, liquibaseInstaller };
		}
	}

	@Installer(description = "DevelopmentModeCondition installer", runCondition = InstallerRunCondition.AlwaysRun)
	static class TestInstaller
	{
		@Autowired
		private DataSource primaryDataSource;

		private boolean executed;

		public boolean isExecuted() {
			return executed;
		}

		public DataSource getPrimaryDataSource() {
			return primaryDataSource;
		}

		@InstallerMethod
		public void execute() {
			executed = true;
		}
	}

	@Installer(description = "Liquibase installer", runCondition = InstallerRunCondition.AlwaysRun)
	static class LiquibaseInstaller extends AcrossLiquibaseInstaller
	{
		@Override
		protected DataSource getDataSource() {
			return super.getDataSource();
		}
	}
}
