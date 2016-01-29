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

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Special bean that takes care of installing the very minimum schema for module installation versioning.
 */
public class AcrossCoreSchemaInstaller
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossCoreSchemaInstaller.class );

	private final DataSource dataSource;
	private final ApplicationContext applicationContext;

	public AcrossCoreSchemaInstaller( DataSource dataSource, ApplicationContext applicationContext ) {
		this.dataSource = dataSource;
		this.applicationContext = applicationContext;
	}

	@PostConstruct
	protected void installCoreSchema() throws LiquibaseException {
		LOG.info( "Installing the core schema for Across" );

		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setChangeLog( "classpath:" + getClass().getName().replace( '.', '/' ) + ".xml" );
		liquibase.setDataSource( dataSource );
		liquibase.setResourceLoader( applicationContext );
		liquibase.afterPropertiesSet();
	}
}
