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
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.across.core.installers.AcrossInstallerRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;
import java.util.Optional;

import static com.foreach.across.core.context.AcrossContextUtils.getApplicationContext;

/**
 * Provides the installer configuration for an AcrossContext.
 * Will also make sure that the core schema is present before bootstrap.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class AcrossInstallerConfig
{
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
	public AcrossInstallerRepository installerRepository( @Qualifier(AcrossContext.DATASOURCE) Optional<DataSource> acrossDataSource,
	                                                      CoreSchemaConfigurationHolder schemaHolder ) {
		if ( !acrossDataSource.isPresent() ) {
			throw new AcrossConfigurationException(
					"Unable to create the AcrossInstallerRepository because there is no DataSource configured. " +
							"A DataSource is required if there is at least one non-disabled installer.",
					"Define a datasource for Across. If you have multiple datasources mark one as @Primary or name the bean 'acrossDataSource'."
			);
		}

		AcrossInstallerRepositoryImpl repository = new AcrossInstallerRepositoryImpl( acrossDataSource.get() );
		repository.setSchema( schemaHolder.getDefaultSchema() );

		return repository;
	}

	@Bean
	@Lazy
	@SuppressWarnings("all")
	public AcrossCoreSchemaInstaller acrossCoreSchemaInstaller( @Qualifier(AcrossContext.INSTALLER_DATASOURCE) Optional<DataSource> installerDataSource,
	                                                            CoreSchemaConfigurationHolder schemaHolder,
	                                                            AcrossContext acrossContext ) {
		if ( !installerDataSource.isPresent() ) {
			throw new AcrossConfigurationException(
					"Unable to create the AcrossCoreSchemaInstaller because there is no DataSource configured. " +
							"A DataSource is required if there is at least one non-disabled installer.",
					"Define a datasource for Across. If you have multiple datasources mark one as @Primary or name the bean 'acrossDataSource'."
			);
		}

		AcrossCoreSchemaInstaller installer = new AcrossCoreSchemaInstaller( installerDataSource.get(), getApplicationContext( acrossContext ) );
		installer.setDefaultSchema( schemaHolder.getDefaultSchema() );

		return installer;
	}
}
