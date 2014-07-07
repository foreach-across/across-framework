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
		DataSource dataSource = acrossDataSource();

		if ( dataSource == null ) {
			throw new AcrossException(
					"Unable to create the AcrossInstallerRepository because there is no DataSource configured.  " +
							"A DataSource is required if there is at least one non-disabled installer."
			);
		}

		return new AcrossInstallerRepository( acrossDataSource() );
	}

	@Bean
	@Lazy
	@DependsOn(AcrossContext.DATASOURCE)
	public AcrossCoreSchemaInstaller acrossCoreSchemaInstaller() {
		DataSource dataSource = acrossDataSource();

		if ( dataSource == null ) {
			throw new AcrossException(
					"Unable to create the AcrossCoreSchemaInstaller because there is no DataSource configured.  " +
							"A DataSource is required if there is at least one non-disabled installer."
			);
		}

		return new AcrossCoreSchemaInstaller( dataSource, AcrossContextUtils.getBeanFactory( acrossContext ) );
	}

	@Bean(name = AcrossContext.DATASOURCE)
	public DataSource acrossDataSource() {
		DataSource dataSource = acrossContext.getDataSource();

		if ( dataSource == null ) {
			LOG.warn( "No Across data source specified - it will be impossible to run any installers." );
		}

		return dataSource;
	}
}
