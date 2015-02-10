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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

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

	/**
	 * Requesting the AcrossInstallerRepository will result in the core schema to be installed.
	 * This bean is lazy because if no installers are required to run, there is no need for a DataSource
	 * and the context can safely bootstrap without.
	 *
	 * @return AcrossInstallerRepository instance.
	 */
	@Bean
	@Lazy
	@DependsOn({ "acrossCoreSchemaInstaller", AcrossContext.DATASOURCE })
	public AcrossInstallerRepository installerRepository() {
		DataSource installerDataSource = acrossInstallerDataSource();

		if ( installerDataSource == null ) {
			throw new AcrossException(
					"Unable to create the AcrossInstallerRepository because there is no DataSource configured.  " +
							"A DataSource is required if there is at least one non-disabled installer."
			);
		}

		return new AcrossInstallerRepository( installerDataSource );
	}

	@Bean
	@Lazy
	@DependsOn(AcrossContext.DATASOURCE)
	public AcrossCoreSchemaInstaller acrossCoreSchemaInstaller() {
		DataSource installerDataSource = acrossInstallerDataSource();

		if ( installerDataSource == null ) {
			throw new AcrossException(
					"Unable to create the AcrossCoreSchemaInstaller because there is no DataSource configured.  " +
							"A DataSource is required if there is at least one non-disabled installer."
			);
		}

		return new AcrossCoreSchemaInstaller( installerDataSource, AcrossContextUtils.getBeanFactory( acrossContext ) );
	}

	@Bean(name = AcrossContext.DATASOURCE)
	public DataSource acrossDataSource() {
		DataSource dataSource = acrossContext.getDataSource();

		if ( dataSource == null ) {
			LOG.warn( "No Across data source specified - it will be impossible to run any installers." );
		}

		return dataSource;
	}

	@Bean(name = AcrossContext.INSTALLER_DATASOURCE)
	@DependsOn(AcrossContext.DATASOURCE)
	public DataSource acrossInstallerDataSource() {
		DataSource installerDataSource = acrossContext.getInstallerDataSource();
		if ( installerDataSource == null ) {
			return acrossDataSource();
		}
		return installerDataSource;
	}
}
