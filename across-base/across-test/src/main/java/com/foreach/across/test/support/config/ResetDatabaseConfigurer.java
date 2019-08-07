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
package com.foreach.across.test.support.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * Takes care of resetting the datasources attached to the {@link com.foreach.across.core.AcrossContext}.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Configuration
public class ResetDatabaseConfigurer implements AcrossContextConfigurer, BeanFactoryAware
{
	private static final Logger LOG = LoggerFactory.getLogger( ResetDatabaseConfigurer.class );

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void configure( AcrossContext context ) {
		DataSource ds = context.getDataSource();
		DataSource installerDs = context.getInstallerDataSource();

		if ( ds != null ) {
			LOG.info( "Resetting database for Across datasource" );
			dropDataSource( ds );
		}

		if ( installerDs != null && installerDs != ds ) {
			LOG.info( "Resetting database for Across installer datasource" );
			dropDataSource( installerDs );
		}
	}

	@SneakyThrows
	private void dropDataSource( DataSource ds ) {
		SpringLiquibase springLiquibase = new SpringLiquibase();
		springLiquibase.setDataSource( ds );
		springLiquibase.setChangeLog( "classpath:com/foreach/across/test/resetDatabase.xml" );
		springLiquibase.setDropFirst( true );

		AutowireCapableBeanFactory bf = (AutowireCapableBeanFactory) beanFactory;
		bf.autowireBean( springLiquibase );
		bf.initializeBean( springLiquibase, "dropDataSource" );
	}
}
