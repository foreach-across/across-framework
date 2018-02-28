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

package com.foreach.across.core.config;

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.across.core.installers.AcrossInstallerRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;

/**
 * Provides the installer configuration for an AcrossContext.
 * Will also make sure that the core schema is present before bootstrap.
 */
@Configuration
public class AcrossInstallerConfig
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossInstallerConfig.class );

	@Autowired
	private AcrossContext acrossContext;

	@Autowired
	private CoreSchemaConfigurationHolder schemaHolder;

	/**
	 * Requesting the AcrossInstallerRepository will result in the core schema to be installed.
	 * This bean is lazy because if no installers are required to run, there is no need for a DataSource
	 * and the context can safely bootstrap without.
	 *
	 * @return AcrossInstallerRepository instance.
	 */
	@Bean
	@Lazy
	@DependsOn("acrossCoreSchemaInstaller")
	public AcrossInstallerRepository installerRepository() {
		DataSource installerDataSource = acrossDataSource();

		if ( installerDataSource == null ) {
			throw new AcrossConfigurationException(
					"Unable to create the AcrossInstallerRepository because there is no DataSource configured. " +
							"A DataSource is required if there is at least one non-disabled installer.",
					"Define a datasource for Across. If you have multiple datasources mark one as @Primary or name the bean 'acrossDataSource'."
			);
		}

		AcrossInstallerRepositoryImpl repository = new AcrossInstallerRepositoryImpl( installerDataSource );
		repository.setSchema( schemaHolder.getDefaultSchema() );

		return repository;
	}

	@Bean
	@Lazy
	@DependsOn(AcrossContext.INSTALLER_DATASOURCE)
	public AcrossCoreSchemaInstaller acrossCoreSchemaInstaller() {
		DataSource installerDataSource = acrossInstallerDataSource();

		if ( installerDataSource == null ) {
			throw new AcrossConfigurationException(
					"Unable to create the AcrossCoreSchemaInstaller because there is no DataSource configured. " +
							"A DataSource is required if there is at least one non-disabled installer.",
					"Define a datasource for Across. If you have multiple datasources mark one as @Primary or name the bean 'acrossDataSource'."
			);
		}

		AcrossCoreSchemaInstaller installer = new AcrossCoreSchemaInstaller(
				installerDataSource, AcrossContextUtils.getApplicationContext( acrossContext )
		);
		installer.setDefaultSchema( schemaHolder.getDefaultSchema() );

		return installer;
	}

	@Bean(name = AcrossContext.INSTALLER_DATASOURCE)
	@DependsOn(AcrossContext.DATASOURCE)
	public DataSource acrossInstallerDataSource() {
		DataSource installerDataSource = acrossContext.getInstallerDataSource();

		if ( installerDataSource == null ) {
			installerDataSource = acrossDataSource();
		}

		if ( installerDataSource == null ) {
			LOG.warn( "No Across installer data source specified - it will be impossible to run any installers." );
		}

		return installerDataSource;
	}

	@Primary
	@Bean(name = AcrossContext.DATASOURCE)
	public DataSource acrossDataSource() {
		return acrossContext.getDataSource();
	}
}
