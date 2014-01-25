package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtil;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.annotation.PostConstruct;

/**
 * Special bean that takes care of installing the very minimum schema for module installation versioning.
 */
public class AcrossCoreSchemaInstaller
{
	private final static Logger LOG = LoggerFactory.getLogger( AcrossCoreSchemaInstaller.class );

	private AcrossContext acrossContext;

	public AcrossCoreSchemaInstaller( AcrossContext acrossContext ) {
		this.acrossContext = acrossContext;
	}

	@PostConstruct
	protected void installCoreSchema() {
		if ( acrossContext.isAllowInstallers() ) {
			LOG.info( "Installing the core schema for Across" );

			AutowireCapableBeanFactory beanFactory = AcrossContextUtil.getBeanFactory( acrossContext );

			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
			liquibase.setDataSource( acrossContext.getDataSource() );

			beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
			beanFactory.initializeBean( liquibase, "" );
		}
		else {
			LOG.info( "Skipping the core schema installer because installers are not allowed." );
		}
	}
}
