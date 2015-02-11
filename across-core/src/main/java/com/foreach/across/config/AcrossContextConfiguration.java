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
package com.foreach.across.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * <p>Creates an AcrossContext bean and will apply all AcrossContextConfigurer instances
 * before bootstrapping.  The configuration of the context is delegated to the configurers.</p>
 * <p>A DataSource bean names acrossDataSource is required.</p>
 */
@Configuration
public class AcrossContextConfiguration
{
	@Autowired(required = false)
	@Qualifier(AcrossContext.DATASOURCE)
	private DataSource dataSource;

	@Autowired(required = false)
	@Qualifier(AcrossContext.INSTALLER_DATASOURCE)
	private DataSource installerDataSource;

	@Autowired
	private Collection<AcrossContextConfigurer> configurers;

	@SuppressWarnings("all")
	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
		AcrossContext context = new AcrossContext( applicationContext );

		// Set installer datasource
		DataSource ds = dataSourceForInstallers();

		if ( ds != null ) {
			context.setInstallerAction( InstallerAction.EXECUTE );
			context.setInstallerDataSource( ds );
		}
		else {
			context.setInstallerAction( InstallerAction.DISABLED );
			System.err.println(
					"No datasource bean named acrossDataSource or acrossInstallerDataSource found - " +
							"configuring a context without datasource and disabling the installers." );
		}

		// Set the context datasource
		context.setDataSource( dataSource );

		for ( AcrossContextConfigurer configurer : configurers ) {
			configurer.configure( context );
		}

		return context;
	}

	private DataSource dataSourceForInstallers() {
		if ( installerDataSource != null ) {
			return installerDataSource;
		}

		return dataSource;
	}
}
