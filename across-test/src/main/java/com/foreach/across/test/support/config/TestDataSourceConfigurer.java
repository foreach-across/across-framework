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
package com.foreach.across.test.support.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.database.DatabaseInfo;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.database.support.HikariDataSourceHelper;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Adds support for test datasource detection to an {@link AcrossContext}.
 * Test datasources can be configured in the <em>${user.home}/dev-configs/across-test.properties</em> with properties
 * of the form:
 * <ul>
 * <li>acrossTest.datasource.NAME.driver</li>
 * <li>acrossTest.datasource.NAME.url</li>
 * <li>acrossTest.datasource.NAME.username</li>
 * <li>acrossTest.datasource.NAME.password</li>
 * </ul>
 * Which datasource to use is determined by the value of the  <strong>acrossTest.datasource</strong> property, that
 * should be set to the <strong>NAME</strong> of the datasource.
 * <p>
 * If no specific datasource is set, this configuration will ensure that a HSQL memory database is used.
 * If the {@link AcrossContext} already has a datasource configured, it will kept.
 * </p>
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@PropertySource(value = "file:${user.home}/dev-configs/across-test.properties", ignoreResourceNotFound = true)
public class TestDataSourceConfigurer implements EnvironmentAware, AcrossContextConfigurer
{
	private Environment environment;

	public void setEnvironment( Environment environment ) {
		this.environment = environment;
	}

	@Override
	public void configure( AcrossContext context ) {
		if ( context.getDataSource() == null ) {
			context.setDataSource( testDataSource() );
		}
		if ( context.getInstallerDataSource() == null ) {
			context.setInstallerDataSource( testDataSource() );
			context.setInstallerAction( InstallerAction.EXECUTE );
		}
	}

	@Bean
	@Lazy
	@SuppressWarnings("all")
	public DataSource testDataSource() {
		HikariDataSource dataSource;

		String dsName = environment.getProperty( "acrossTest.datasource" );

		if ( dsName == null ) {
			dsName = environment.getProperty( "acrossTest.datasource.default", "auto" );
		}

		System.out.println( "Creating Across test datasource with profile: " + dsName );

		if ( StringUtils.equals( "auto", dsName ) ) {
			dataSource = HikariDataSourceHelper.create( "org.hsqldb.jdbc.JDBCDriver",
			                                            "jdbc:hsqldb:mem:/hsql-mem/across-test", "sa",
			                                            StringUtils.EMPTY );
		}
		else {
			dataSource = HikariDataSourceHelper.create(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".driver" ),
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".url" ),
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".username" ),
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".password" ) );
			if ( dataSource.getJdbcUrl().startsWith( "jdbc:jtds:" ) ) {
				// jtds is not JDBC 4.0 compliant
				dataSource.setConnectionTestQuery( "select 1" );
			}
		}

		DatabaseInfo databaseInfo = DatabaseInfo.retrieve( dataSource );
		System.out.println(
				"Connection to " + databaseInfo.getProductName() + " - version: " + databaseInfo.getProductVersion() );

		return dataSource;
	}
}
