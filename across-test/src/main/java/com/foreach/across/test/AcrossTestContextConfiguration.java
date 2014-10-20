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

package com.foreach.across.test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.cache.AcrossCompositeCacheManager;
import com.foreach.across.core.database.DatabaseInfo;
import com.foreach.across.core.installers.InstallerAction;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@PropertySource(value = "file:${user.home}/dev-configs/across-test.properties", ignoreResourceNotFound = true)
public class AcrossTestContextConfiguration implements EnvironmentAware
{
	private Environment environment;

	public void setEnvironment( Environment environment ) {
		this.environment = environment;
	}

	@Bean
	@SuppressWarnings("all")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();

		String dsName = System.getProperty( "acrossTest.datasource", null );

		if ( dsName == null ) {
			dsName = environment.getProperty( "acrossTest.datasource.default", "auto" );
		}

		System.out.println( "Creating Across test datasource with profile: " + dsName );

		if ( StringUtils.equals( "auto", dsName ) ) {
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/across-test" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );
		}
		else {
			dataSource.setDriverClassName(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".driver" ) );
			dataSource.setUrl( environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".url" ) );
			dataSource.setUsername(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".username" ) );
			dataSource.setPassword(
					environment.getRequiredProperty( "acrossTest.datasource." + dsName + ".password" ) );
		}

		DatabaseInfo databaseInfo = DatabaseInfo.retrieve( dataSource );
		System.out.println(
				"Connection to " + databaseInfo.getProductName() + " - version: " + databaseInfo.getProductVersion() );

		return dataSource;
	}

	@Bean
	public SpringLiquibase databaseReset() {
		SpringLiquibase springLiquibase = new SpringLiquibase();
		springLiquibase.setDataSource( dataSource() );
		springLiquibase.setChangeLog( "classpath:com/foreach/across/test/resetDatabase.xml" );
		springLiquibase.setDropFirst( true );

		return springLiquibase;
	}

	@Bean
	@Primary
	public CacheManager cacheManager( AcrossContext acrossContext ) {
		return new AcrossCompositeCacheManager( acrossContext.isDisableNoOpCacheManager() );
	}

	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
		databaseReset();

		Map<String, AcrossContextConfigurer> configurerMap =
				applicationContext.getBeansOfType( AcrossContextConfigurer.class );

		AcrossContext context = new AcrossContext( applicationContext );
		context.setInstallerAction( InstallerAction.EXECUTE );
		context.setDataSource( dataSource() );

		for ( AcrossContextConfigurer configurer : configurerMap.values() ) {
			configurer.configure( context );
		}

		return context;
	}
}
