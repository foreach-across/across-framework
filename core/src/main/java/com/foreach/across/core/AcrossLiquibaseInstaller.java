package com.foreach.across.core;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.sql.DataSource;

@InstallerGroup(InstallerGroup.SCHEMA)
public abstract class AcrossLiquibaseInstaller
{
	@Autowired
	private AcrossContext acrossContext;

	@Autowired
	@Qualifier(AcrossContext.DATASOURCE)
	private DataSource dataSource;

	private final String changelog;

	protected AcrossLiquibaseInstaller() {
		changelog = "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml";
	}

	protected AcrossLiquibaseInstaller( String changelog ) {
		this.changelog = changelog;
	}

	@InstallerMethod
	public void install() {
		AutowireCapableBeanFactory beanFactory = acrossContext.getApplicationContext().getAutowireCapableBeanFactory();

		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog( changelog );
		liquibase.setDataSource( dataSource );

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( liquibase, "" );
	}
}
