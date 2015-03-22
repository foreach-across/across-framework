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
package com.foreach.across.test.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.config.AcrossInstallerConfig;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;

/**
 * @author Andy Somers
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(
		classes = { AcrossInstallerConfig.class, TestAcrossInstallerConfigWithoutInstallerDataSource.Config.class })
public class TestAcrossInstallerConfigWithoutInstallerDataSource
{
	@Autowired
	private AcrossCoreSchemaInstaller acrossCoreSchemaInstaller;

	@Test
	public void acrossCoreSchemaInstallerUsesDefaultDataSourceIfConfigured() throws Exception {
		assertNotNull( acrossCoreSchemaInstaller );
		Object dataSource = ReflectionTestUtils.getField( acrossCoreSchemaInstaller, "dataSource" );
		assertNotNull( dataSource );
	}

	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Bean
		public AcrossContext acrossContext() {
			AcrossContext ctx = new AcrossContext();
			ctx.bootstrap();
			return ctx;
		}

		@Bean
		public DataSource acrossDataSource() {
			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setJdbcUrl( "jdbc:hsqldb:mem:acrossTest" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );
			return dataSource;
		}

		@Override
		public void configure( AcrossContext context ) {

		}
	}
}
