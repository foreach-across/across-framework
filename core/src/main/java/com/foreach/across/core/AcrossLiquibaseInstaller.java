package com.foreach.across.core;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

public abstract class AcrossLiquibaseInstaller extends AcrossInstaller
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	@Qualifier("installDataSource")
	private DataSource dataSource;

	private final String changelog;

	protected AcrossLiquibaseInstaller() {
		changelog = "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml";
	}

	protected AcrossLiquibaseInstaller( String changelog ) {
		this.changelog = changelog;
	}

	@Override
	protected void install() {
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog( changelog );
		liquibase.setDataSource( dataSource );

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false );
		beanFactory.initializeBean( liquibase, "" );
	}
}
