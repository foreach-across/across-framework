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

import com.foreach.across.config.AcrossContextConfiguration;
import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * @author Andy Somers
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestContextWithoutInstallerDataSource.Config.class)
public class TestContextWithoutInstallerDataSource
{
	@Autowired
	private AcrossContext acrossContext;

	@Test
	public void contextWithoutInstallerDataSourceSetsDefaultDataSourceAsInstallerDataSourceOnConfig() {
		DataSource dataSource = acrossContext.getDataSource();
		DataSource installerDataSource = acrossContext.getInstallerDataSource();

		assertNotNull( dataSource );
		assertNotNull( installerDataSource );
		assertSame( dataSource, installerDataSource );
	}

	@Configuration
	@EnableAcrossContext
	static class Config implements AcrossContextConfigurer
	{
		@Bean
		public AcrossContextConfiguration acrossContextConfiguration() {
			return new AcrossContextConfiguration();
		}

		@Bean
		public DataSource acrossDataSource() {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:acrossTest" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );
			return dataSource;
		}

		@Override
		public void configure( AcrossContext context ) {

		}
	}
}