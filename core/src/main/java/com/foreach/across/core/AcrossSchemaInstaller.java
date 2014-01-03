package com.foreach.across.core;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

public abstract class AcrossSchemaInstaller extends SpringLiquibase
{
	@Autowired
	private AcrossContext context;

	@Autowired
	@Qualifier("installDataSource")
	private DataSource dataSource;

	protected AcrossSchemaInstaller() {
		String changelog = "classpath:" + getClass().getPackage().getName().replace( '.',
		                                                                             '/' ) + "/liquibase/" + getClass().getSimpleName() + ".xml";
		setChangeLog( changelog );

	}

	@Override
	public void afterPropertiesSet() throws LiquibaseException {
		if ( context.isAllowInstallers() ) {
			if ( getResourceLoader().getResource( getChangeLog() ).exists() ) {
				if ( getDataSource() == null ) {
					setDataSource( dataSource );
				}

				super.afterPropertiesSet();
			}
		}
	}
}
