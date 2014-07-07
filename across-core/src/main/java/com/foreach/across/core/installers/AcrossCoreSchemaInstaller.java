package com.foreach.across.core.installers;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Special bean that takes care of installing the very minimum schema for module installation versioning.
 */
public class AcrossCoreSchemaInstaller
{
	private final static Logger LOG = LoggerFactory.getLogger( AcrossCoreSchemaInstaller.class );

	private final DataSource dataSource;
	private final AutowireCapableBeanFactory beanFactory;

	public AcrossCoreSchemaInstaller( DataSource dataSource, AutowireCapableBeanFactory beanFactory ) {
		this.dataSource = dataSource;
		this.beanFactory = beanFactory;
	}

	@PostConstruct
	protected void installCoreSchema() {
		LOG.info( "Installing the core schema for Across" );

		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
		liquibase.setDataSource( dataSource );

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( liquibase, "" );
	}
}
