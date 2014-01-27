package com.foreach.across.core;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.SpringContextRefreshedEventListener;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Contains the base configuration for Across.
 */
@Configuration
public class AcrossConfig
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

	@Bean
	public AcrossEventPublisher eventPublisher() {
		return new AcrossEventPublisher();
	}

	@Bean
	public SpringContextRefreshedEventListener refreshedEventListener() {
		return new SpringContextRefreshedEventListener();
	}
}
