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

import jakarta.annotation.PostConstruct;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

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
		// It is incredibly difficult to determine whether a table exists or not, when you have to support all database vendors and releases:
		// Just see liquibase.precondition.core.TableExistsPrecondition.
		// This is why we just run the select, and if that fails, we assume we need to run the liquibase databaseChangeLog,
		// which will do the real check.
		try {
			return jdbcTemplate.queryForObject(
					applySchema( AcrossInstallerRepositoryImpl.SQL_SELECT_VERSION ),
					Integer.class,
					MODULE_NAME,
					INSTALLER_NAME
			);
		}
		catch ( Exception e ) {
			// We need some logging for this exception, because in most cases, the query above will be the very first one that gets run,
			// and if that fails for an exotic reason, we need those details.
			// By default, we don't log the exception stack trace in the warning, because that pollutes the logs for the integration tests too much.
			// The message and classname of the exception should give a sufficient hint in most cases.
			// You can however enable the trace level for this specific logger to get the exception stack trace if you really need it.
			if ( LOG.isTraceEnabled() ) {
				// Even though we check whether the trace logging is enabled, we still log it as a warning:
				LOG.warn( "Assuming {} needs to run, because there was an exception retrieving its version from ACROSSMODULES:",
				          INSTALLER_NAME, e );
			}
			else {
				Throwable cause = e.getCause();
				LOG.warn( "Assuming {} needs to run, because there was an exception retrieving its version from ACROSSMODULES: {}: {}; Cause: {}: {}",
				          INSTALLER_NAME, e.getClass().getName(), e.getMessage(),
				          cause != null ? cause.getClass().getName() : null, cause != null ? cause.getMessage() : null );
			}
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
