package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossContext;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Special bean that takes care of installing the very minimum schema for module installation versioning.
 */
@Component("AcrossCoreSchemaInstaller")
public class AcrossCoreSchemaInstaller
{
	private final static Logger LOG = LoggerFactory.getLogger( AcrossCoreSchemaInstaller.class );

	@Autowired
	private AcrossContext acrossContext;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	@Qualifier("installDataSource")
	private DataSource dataSource;

	@PostConstruct
	protected void installCoreSchema() {
		if ( acrossContext.isAllowInstallers() && !acrossContext.isSkipSchemaInstallers() ) {
			AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
			liquibase.setDataSource( dataSource );

			beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false );
			beanFactory.initializeBean( liquibase, "" );
		}
		else {
			LOG.info( "Skipping the core schema installer because installers or schema installers are not allowed." );
		}
	}
}
