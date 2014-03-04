package com.foreach.across.core;

import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Provides the installer configuration for an AcrossContext.
 * Will also make sure that the core schema is present before bootstrap.
 */
@Configuration
public class AcrossInstallerConfig
{
	@Autowired
	private AcrossContext acrossContext;

	@Bean(name = AcrossContext.DATASOURCE)
	public DataSource acrossDataSource() {
		return acrossContext.getDataSource();
	}

	@Bean
	public AcrossCoreSchemaInstaller acrossCoreSchemaInstaller() {
		return new AcrossCoreSchemaInstaller( acrossContext );
	}

	@Bean
	@DependsOn({ "acrossCoreSchemaInstaller", AcrossContext.DATASOURCE })
	public AcrossInstallerRepository installerRepository() {
		return new AcrossInstallerRepository( acrossDataSource() );
	}

}
