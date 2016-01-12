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
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestMultipleDataSources.Config.class)
public class TestMultipleDataSources
{
	private JdbcTemplate core;

	@Autowired
	private void setAcrossDataSource( EmbeddedDatabase acrossDataSource ){
		core = new JdbcTemplate( acrossDataSource );
	}

	@Test
	public void bootstrapLockShouldBeCreatedButNoLongerHeld() {
		assertEquals(
				Integer.valueOf( 1 ),
				core.queryForObject(
						"SELECT count(*) FROM PUBLIC.across_locks WHERE lock_id = 'across:bootstrap' AND holds = 0",
						Integer.class
				)
		);
	}

	@Test
	public void installerRecordShouldBeCreated() {
		assertEquals(
			Integer.valueOf( 1 ),
		    core.queryForObject(
				    "SELECT count(*) FROM acrossmodules WHERE installer_id = '" + MyInstaller.class.getName() + "'",
		        Integer.class
		    )
		);
	}

	@Test
	public void installerTableShouldBeCreatedInRightDataSource() {

	}

	@Configuration
	@EnableAcrossContext
	static class Config
	{
		@Bean
		public InstallerModule installerModule() {
			return new InstallerModule();
		}

		@Bean
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "core" )
					.addScript( "scripts/hsqldb-schema.sql" )
					.build();
		}
	}

	static class InstallerModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "InstallerModule";
		}

		@Override
		public String getDescription() {
			return "Only has a single installer.";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { MyInstaller.class };
		}
	}

	@Installer(description = "Creates a simple table in the datasource.", version = 1)
	static class MyInstaller extends AcrossLiquibaseInstaller
	{
		@InstallerMethod
		public void install() {

		}
	}
}
