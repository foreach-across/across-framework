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
import java.nio.file.Path;
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
	private boolean shouldRun = true;
	private Path rollbackFile;
	private SpringLiquibase liquibaseConfiguration = new SpringLiquibase();

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the changelog file which corresponds to this installers full class name
	 * to perform the liquibase update
	 * <p/>
	 * eg. if this constructor is used to create an instance of the class my.example.LiquibaseInstaller
	 * the changelog file at {resourceRoot}/my/example/LiquibaseInstaller.xml will be used to preform the liquibase update
	 */
	protected AcrossLiquibaseInstaller() {
		liquibaseConfiguration.setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
	}

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the changelog file which corresponds to this installers full class name
	 * to perform the liquibase update
	 * and will construct the {@link liquibase.changelog.ChangeLogParameters} based on the given {@link SchemaConfiguration}
	 * <p />
	 * eg. if this constructor is used to create an instance of the class my.example.LiquibaseInstaller
	 * the changelog file at {resourceRoot}/my/example/LiquibaseInstaller.xml will be used to preform the update
	 */
	protected AcrossLiquibaseInstaller( SchemaConfiguration schemaConfiguration ) {
		this();
		this.schemaConfiguration = schemaConfiguration;
	}

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the given changelog file to perform the liquibase update
	 */
	protected AcrossLiquibaseInstaller( String changelog ) {
		liquibaseConfiguration.setChangeLog( changelog );
	}

	/**
	 * Creates an {@link AcrossLiquibaseInstaller} which will use the given changelog file to perform the liquibase update
	 * and will construct the {@link liquibase.changelog.ChangeLogParameters} based on the given {@link SchemaConfiguration}
	 */
	protected AcrossLiquibaseInstaller( String changelog, SchemaConfiguration schemaConfiguration ) {
		liquibaseConfiguration.setChangeLog( changelog );
		this.schemaConfiguration = schemaConfiguration;
	}

	/**
	 * Sets the {@link SchemaConfiguration} that will be used to construct the {@link liquibase.changelog.ChangeLogParameters}
	 *
	 * @param schemaConfiguration
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
	 */
	protected void setChangelog( String changelog ) {
		liquibaseConfiguration.setChangeLog( changelog );
	}

	/**
	 * @return a Resource that is able to resolve to a file or classpath resource.
	 */
	protected String getChangelog() {
		return liquibaseConfiguration.getChangeLog();
	}

	/**
	 * Sets the default Schema that will be used during the liquibase update
	 * <p/>
	 * This will override the globally configured default schema
	 *
	 * @param defaultSchema The default db schame name
	 * @see
	 */
	protected void setDefaultSchema( String defaultSchema ) {
		liquibaseConfiguration.setDefaultSchema( defaultSchema );
	}

	/**
	 * @return The db shame name that will be used as default schema during the liquibase update
	 */
	protected String getDefaultSchema() {
		return liquibaseConfiguration.getDefaultSchema();
	}

	/**
	 * Sets the contexts string that will be used to construct the {@link liquibase.Contexts} object
	 *
	 * @param contexts
	 */
	protected void setContexts( String contexts ) {
		liquibaseConfiguration.setContexts( contexts );
	}

	/**
	 * @return the contexts string that will be used to construct the {@link liquibase.Contexts} object
	 */
	protected String getContexts() {
		return liquibaseConfiguration.getContexts();
	}

	/**
	 * Sets the dropFirst property
	 * if this property is set to true, liquibase will first drop all database objects owned by the current user, before updating
	 * if this property is set to false, liquibase will not drop all database objects owned by the current user, before updating
	 *
	 * @param dropFirst
	 */
	protected void setDropFirst( boolean dropFirst ) {
		liquibaseConfiguration.setDropFirst( dropFirst );
	}

	/**
	 * @return the dropFirst property
	 */
	protected boolean isDropFirst() {
		return liquibaseConfiguration.isDropFirst();
	}

	/**
	 * Sets the shouldRun property
	 * if this property is set to true, the liquibase update will run on installation
	 * if this property is set to false, the liquibase update will not run on installation
	 *
	 * @param shouldRun
	 */
	protected void setShouldRun( boolean shouldRun ) {
		this.shouldRun = shouldRun;
	}

	/**
	 * @return the shouldRun property
	 */
	protected boolean isShouldRun() {
		return shouldRun;
	}

	/**
	 * Ignores classpath prefix during changeset comparison.
	 * This is particularly useful if Liquibase is run in different ways.
	 *
	 * @param ignoreClasspathPrefix
	 * @see {@link SpringLiquibase#setIgnoreClasspathPrefix(boolean)}
	 */
	protected void setIgnoreClasspathPrefix( boolean ignoreClasspathPrefix ) {
		liquibaseConfiguration.setIgnoreClasspathPrefix( ignoreClasspathPrefix );
	}

	/**
	 * @return the ignoreClassPathPrefix property
	 */
	protected boolean isIgnoreClasspathPrefix() {
		return liquibaseConfiguration.isIgnoreClasspathPrefix();
	}

	/**
	 * Sets the file where liquibase will write the rollback SQL to updating
	 * <p/>
	 * No rollback sql will be generated if the rollbackFile is null
	 *
	 * @param rollbackFile
	 */
	protected void setRollbackFile( Path rollbackFile ) {
		this.rollbackFile = rollbackFile;
	}

	/**
	 * @return the file where liquibase will write the rollback sql to
	 */
	protected Path getRollbackFile() {
		return rollbackFile;
	}

	/**
	 * Override the dataSource this installer should use (defaults to the installer datasource otherwise).
	 *
	 * @param dataSource instance
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

		SpringLiquibase liquibase = buildSpringLiquibase();

		beanFactory.autowireBeanProperties( liquibase, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( liquibase, "" );
	}

	private SpringLiquibase buildSpringLiquibase() {
		SpringLiquibase liquibase = new SpringLiquibase();

		liquibase.setChangeLogParameters( buildParameters( schemaConfiguration ) );
		liquibase.setChangeLog( liquibase.getChangeLog() );
		liquibase.setDefaultSchema( liquibaseConfiguration.getDefaultSchema() );
		liquibase.setContexts( liquibaseConfiguration.getContexts() );
		liquibase.setDropFirst( liquibaseConfiguration.isDropFirst() );
		liquibase.setShouldRun( shouldRun );
		liquibase.setDataSource( dataSource );

		if ( rollbackFile != null ) {
			liquibase.setRollbackFile( rollbackFile.toFile() );
		}

		return liquibase;
	}

	private Map<String, String> buildParameters( SchemaConfiguration schemaConfiguration ) {
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
