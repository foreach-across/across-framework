package com.foreach.across.core.installers;

import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtil;
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
		AutowireCapableBeanFactory beanFactory = AcrossContextUtil.getBeanFactory( acrossContext );

		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog( changelog );
		liquibase.setDataSource( dataSource );

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( liquibase, "" );
	}
}
