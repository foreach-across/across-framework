/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossContext;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the base for Installers which run liquibase update on installation
 *
 * @author Arne Vandamme
 * @author Andy Debrouwer
 */
@InstallerGroup(InstallerGroup.SCHEMA)
public abstract class AcrossLiquibaseInstaller
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossLiquibaseInstaller.class );

	@Autowired
	private AcrossContextInfo acrossContext;

	@Autowired
	@Qualifier(AcrossContext.INSTALLER_DATASOURCE)
	private DataSource dataSource;

	private SchemaConfiguration schemaConfiguration;
	private String defaultSchema;
	private String changeLog;

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the changelog file which corresponds to this installers full class name
	 * to perform the liquibase update
	 * <p/>
	 * eg if this constructor is used to create an instance of the class {@code my.example.LiquibaseInstaller}
	 * the changelog file at {@code {resourceRoot}/my/example/LiquibaseInstaller.xml} will be used to preform the liquibase update
	 */
	protected AcrossLiquibaseInstaller() {
		setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
	}

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the changelog file which corresponds to this installers full class name
	 * to perform the liquibase update
	 * and will construct the {@link liquibase.changelog.ChangeLogParameters} based on the given {@link SchemaConfiguration}
	 * <p/>
	 * eg if this constructor is used to create an instance of the class {@code my.example.LiquibaseInstaller }
	 * the changelog file at {@code {resourceRoot}/my/example/LiquibaseInstaller.xml} will be used to preform the update
	 */
	protected AcrossLiquibaseInstaller( SchemaConfiguration schemaConfiguration ) {
		this();
		setSchemaConfiguration( schemaConfiguration );
	}

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the given changelog file to perform the liquibase update
	 */
	protected AcrossLiquibaseInstaller( String changelog ) {
		setChangeLog( changelog );
	}

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the given changelog file to perform the liquibase update
	 * and will construct the {@link liquibase.changelog.ChangeLogParameters} based on the given {@link SchemaConfiguration}
	 */
	protected AcrossLiquibaseInstaller( String changelog, SchemaConfiguration schemaConfiguration ) {
		setChangeLog( changelog );
		setSchemaConfiguration( schemaConfiguration );
	}

	/**
	 * Sets the {@link SchemaConfiguration} that will be used to construct the {@link liquibase.changelog.ChangeLogParameters}
	 * <p/>
	 * if no defaultSchema is configured via {@link AcrossLiquibaseInstaller#setDefaultSchema(String)}
	 * the defaultSchema from {@link SchemaConfiguration#getDefaultSchema()} will be used instead
	 *
	 * @param schemaConfiguration the {@link SchemaConfiguration} to be set
	 *
	 * @see liquibase.integration.spring.SpringLiquibase#setChangeLogParameters(Map)
	 */
	protected void setSchemaConfiguration( SchemaConfiguration schemaConfiguration ) {
		this.schemaConfiguration = schemaConfiguration;
	}

	/**
	 * @return the {@link SchemaConfiguration} that will be used to construct the {@link liquibase.changelog.ChangeLogParameters}
	 */
	protected SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}

	/**
	 * Sets a Spring Resource that is able to resolve to a file or classpath resource.
	 * An example might be <code>classpath:db-changelog.xml</code>.
	 *
	 * @see liquibase.integration.spring.SpringLiquibase#setChangeLog(String)
	 */
	public void setChangeLog( String changelog ) {
		this.changeLog = changelog;
	}

	/**
	 * @return a Resource that is able to resolve to a file or classpath resource.
	 */
	protected String getChangelog() {
		return changeLog;
	}

	/**
	 * Sets the default Schema that will be used during the liquibase update
	 * <p/>
	 * This will override the defaultSchema configured in {@link SchemaConfiguration#getDefaultSchema()}
	 *
	 * @param defaultSchema The default db schema name
	 * @see liquibase.integration.spring.SpringLiquibase#setDefaultSchema(String)
	 */
	protected void setDefaultSchema( String defaultSchema ) {
		this.defaultSchema = defaultSchema;
	}

	/**
	 * @return The db schema name that will be used as default schema during the liquibase update
	 */
	protected String getDefaultSchema() {
		return defaultSchema;
	}

	/**
	 * Override the dataSource this installer should use (defaults to the installer datasource otherwise).
	 *
	 * @param dataSource the {@link javax.sql.DataSource} instance to be set
	 *
	 * @see liquibase.integration.spring.SpringLiquibase#setDataSource(DataSource)
	 */
	protected void setDataSource( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	/**
	 * @return The DataSource that liquibase will use to perform the migration.
	 */
	protected DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * This is this {@link AcrossLiquibaseInstaller}s {@link InstallerMethod}
	 * <p/>
	 * It will create a {@link SpringLiquibase} instance based on this {@link AcrossLiquibaseInstaller}s configuration and run a liquibase update
	 */
	@InstallerMethod
	public void install() {
		AutowireCapableBeanFactory beanFactory = AcrossContextUtils.getBeanFactory( acrossContext.getContext() );

		SpringLiquibase springLiquibase = new SpringLiquibase();
		SpringLiquibase liquibase = configureSpringLiquibase( springLiquibase );

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( liquibase, "" );
	}

	/**
	 * This method configures the {@link SpringLiquibase} object before running the liquibase installer.
	 * <p/>
	 * This implementation will set the {@link SpringLiquibase} objects changelogParameters, changelog, default schema and data source
	 * based on this {@link AcrossLiquibaseInstaller}s configuration
	 * <p/>
	 * This method can be overwritten if you need to perform some extra configuration on the {@link SpringLiquibase} object
	 *
	 * @param springLiquibase
	 * @return The configured @link SpringLiquibase} object
	 */
	protected SpringLiquibase configureSpringLiquibase( SpringLiquibase springLiquibase ) {
		springLiquibase.setChangeLogParameters( buildParameters( getSchemaConfiguration() ) );
		springLiquibase.setChangeLog( getChangelog() );

		if ( StringUtils.isNotEmpty( getDefaultSchema() ) ) {
			springLiquibase.setDefaultSchema( getDefaultSchema() );
		}
		else if ( getSchemaConfiguration() != null ) {
			springLiquibase.setDefaultSchema( getSchemaConfiguration().getDefaultSchema() );
		}

		springLiquibase.setDataSource( getDataSource() );

		return springLiquibase;
	}

	protected Map<String, String> buildParameters( SchemaConfiguration schemaConfiguration ) {
		if ( schemaConfiguration == null ) {
			return Collections.emptyMap();
		}

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
