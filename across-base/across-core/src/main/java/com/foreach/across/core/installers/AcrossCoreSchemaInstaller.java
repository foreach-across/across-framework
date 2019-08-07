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

package com.foreach.across.core.installers;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Date;

/**
 * Special bean that takes care of installing the very minimum schema for module installation versioning.
 */
public class AcrossCoreSchemaInstaller
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossCoreSchemaInstaller.class );

	private static final int EXPECTED_VERSION = 1;
	private static final String MODULE_NAME = "Across";
	private static final String INSTALLER_NAME = "AcrossCoreSchemaInstaller";

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	private final ApplicationContext applicationContext;

	private String defaultSchema;

	public AcrossCoreSchemaInstaller( DataSource dataSource, ApplicationContext applicationContext ) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate( dataSource );
		this.applicationContext = applicationContext;
	}

	public void setDefaultSchema( String defaultSchema ) {
		this.defaultSchema = defaultSchema;
	}

	@PostConstruct
	protected void installCoreSchema() throws LiquibaseException {
		int installedVersion = getInstalledVersion();

		if ( installedVersion != EXPECTED_VERSION ) {
			LOG.info( "Installing the core schema for Across" );

			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
			liquibase.setDataSource( dataSource );
			liquibase.setResourceLoader( applicationContext );

			if ( defaultSchema != null ) {
				liquibase.setDefaultSchema( defaultSchema );
			}

			liquibase.afterPropertiesSet();

			updateInstalledVersion( EXPECTED_VERSION );
		}
		else {
			LOG.debug( "Core schema for Across already installed, version {}", EXPECTED_VERSION );
		}
	}

	private int getInstalledVersion() {
		try {
			return jdbcTemplate.queryForObject(
					applySchema( AcrossInstallerRepositoryImpl.SQL_SELECT_VERSION ),
					Integer.class,
					MODULE_NAME,
					INSTALLER_NAME
			);
		}
		catch ( Exception ignore ) {
			return -1;
		}
	}

	private void updateInstalledVersion( int actualVersion ) {
		// Fetch again in case of concurrent deploys
		int previousVersion = getInstalledVersion();

		if ( previousVersion != -1 ) {
			jdbcTemplate.update(
					applySchema( AcrossInstallerRepositoryImpl.SQL_UPDATE_VERSION ),
					actualVersion,
					"Installs the core Across schema",
					new Date(),
					MODULE_NAME,
					INSTALLER_NAME
			);
		}
		else {
			jdbcTemplate.update(
					applySchema( AcrossInstallerRepositoryImpl.SQL_INSERT_VERSION ),
					MODULE_NAME,
					MODULE_NAME,
					INSTALLER_NAME,
					INSTALLER_NAME,
					actualVersion,
					new Date(),
					"Installs the core Across schema"
			);
		}
	}

	private String applySchema( String sql ) {
		return StringUtils.replace( sql, "{schema}", defaultSchema != null ? defaultSchema + "." : "" );
	}
}
