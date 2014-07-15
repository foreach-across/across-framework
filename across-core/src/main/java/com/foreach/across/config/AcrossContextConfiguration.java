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
	@Qualifier("acrossDataSource")
	private DataSource dataSource;

	@Autowired
	private Collection<AcrossContextConfigurer> configurers;

	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
		AcrossContext context = new AcrossContext( applicationContext );

		if ( dataSource != null ) {
			context.setInstallerAction( InstallerAction.EXECUTE );
			context.setDataSource( dataSource );
		}
		else {
			context.setInstallerAction( InstallerAction.DISABLED );
			System.err.println(
					"No datasource bean named acrossDataSource found - configuring a context without datasource and disabling the installers." );
		}

		for ( AcrossContextConfigurer configurer : configurers ) {
			configurer.configure( context );
		}

		return context;
	}
}
