package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.database.SchemaObject;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@InstallerGroup(InstallerGroup.SCHEMA)
public abstract class AcrossLiquibaseInstaller
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossLiquibaseInstaller.class );

	@Autowired
	private AcrossContextInfo acrossContext;

	@Autowired
	@Qualifier(AcrossContext.DATASOURCE)
	private DataSource dataSource;

	private String changelog;
	private SchemaConfiguration schemaConfiguration;

	protected AcrossLiquibaseInstaller() {
		this.changelog = "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml";
	}

	protected AcrossLiquibaseInstaller( SchemaConfiguration schemaConfiguration ) {
		this();
		this.schemaConfiguration = schemaConfiguration;
	}

	protected AcrossLiquibaseInstaller( String changelog ) {
		this.changelog = changelog;
	}

	protected AcrossLiquibaseInstaller( String changelog, SchemaConfiguration schemaConfiguration ) {
		this.changelog = changelog;
		this.schemaConfiguration = schemaConfiguration;
	}

	protected SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}

	protected void setSchemaConfiguration( SchemaConfiguration schemaConfiguration ) {
		this.schemaConfiguration = schemaConfiguration;
	}

	@InstallerMethod
	public void install() {
		AutowireCapableBeanFactory beanFactory = AcrossContextUtils.getBeanFactory( acrossContext.getContext() );

		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog( changelog );
		liquibase.setDataSource( dataSource );

		if ( schemaConfiguration != null ) {
			liquibase.setChangeLogParameters( buildParameters( schemaConfiguration ) );
		}

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( liquibase, "" );
	}

	private Map<String, String> buildParameters( SchemaConfiguration schemaConfiguration ) {
		Map<String, String> parameters = new HashMap<>();

		for ( SchemaObject object : schemaConfiguration.getTables() ) {
			if ( !StringUtils.equals( object.getOriginalName(), object.getCurrentName() ) ) {
				LOG.debug( "Schema property: rename table {} to {}", object.getOriginalName(),
				           object.getCurrentName() );
			}

			parameters.put( object.getKey(), object.getCurrentName() );
		}

		parameters.putAll( schemaConfiguration.getProperties() );

		if ( LOG.isDebugEnabled() ) {
			for ( Map.Entry<String, String> parameter : parameters.entrySet() ) {
				LOG.debug( "Liquibase parameter: {} - {}", parameter.getKey(), parameter.getValue() );
			}
		}

		return parameters;
	}
}
