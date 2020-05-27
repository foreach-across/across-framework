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
package com.foreach.across.core.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.foreach.across.core.AcrossContext.DATASOURCE;
import static com.foreach.across.core.AcrossContext.INSTALLER_DATASOURCE;

/**
 * Responsible for registering the Across datasource related bean definitions.
 * Will register {@link com.foreach.across.core.AcrossContext#DATASOURCE} and {@link com.foreach.across.core.AcrossContext#INSTALLER_DATASOURCE}
 * if necessary.  If there is no separate installer datasource, it will be an alias for the default Across datasource.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Slf4j
class AcrossDataSourceRegistrar implements BeanDefinitionRegistryPostProcessor
{
	@Override
	public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
		AcrossListableBeanFactory beanFactory = (AcrossListableBeanFactory) registry;
		AcrossContext acrossContext = (AcrossContext) beanFactory.getSingleton( AcrossContext.BEAN );

		boolean hasInstallerDataSource = acrossContext.getInstallerDataSource() != null
				&& acrossContext.getDataSource() != acrossContext.getInstallerDataSource();

		if ( !beanFactory.containsBeanDefinition( DATASOURCE ) ) {
			registerPrimaryDataSource( acrossContext.getDataSource(), beanFactory, hasInstallerDataSource );
		}

		if ( !beanFactory.containsBeanDefinition( INSTALLER_DATASOURCE ) ) {
			if ( hasInstallerDataSource ) {
				registerInstallerDataSource( acrossContext.getInstallerDataSource(), beanFactory );
			}
			else {
				BeanDefinitionRegistry primaryRegistry = getRegistryWithLocalBeanDefinition( beanFactory, DATASOURCE );
				if ( primaryRegistry != null ) {
					primaryRegistry.registerAlias( DATASOURCE, INSTALLER_DATASOURCE );
					if( primaryRegistry instanceof AbstractBeanFactory ) {
						// getAliases() would fail because this gets cached before the registrar is called
						(( AbstractBeanFactory) primaryRegistry).clearMetadataCache();
					}
				}
			}
		}
	}

	private void registerPrimaryDataSource( DataSource expected, AcrossListableBeanFactory beanFactory, boolean hasInstallerDataSource ) {
		BeanDefinitionRegistry registry = getRegistryWithLocalBeanDefinition( beanFactory, DATASOURCE );

		if ( registry != null ) {
			BeanDefinition beanDefinition = registry.getBeanDefinition( DATASOURCE );
			boolean multipleDataSources = hasMultipleDataSources( beanFactory ) || hasInstallerDataSource;

			if ( multipleDataSources && !beanDefinition.isPrimary() ) {
				registerPrimaryDataSourceBeanDefinition( beanFactory, expected );
			}
		}

		else if ( expected != null ) {
			registerPrimaryDataSourceBeanDefinition( beanFactory, expected );
		}
	}

	private void registerInstallerDataSource( DataSource installerDataSource, AcrossListableBeanFactory beanFactory ) {
		BeanDefinitionRegistry registry = getRegistryWithLocalBeanDefinition( beanFactory, INSTALLER_DATASOURCE );

		if ( registry == null ) {
			GenericBeanDefinition definition = new GenericBeanDefinition();
			definition.setPrimary( false );
			definition.setBeanClass( DataSource.class );
			beanFactory.registerBeanDefinition( INSTALLER_DATASOURCE, definition );
			beanFactory.registerSingleton( INSTALLER_DATASOURCE, installerDataSource );
		}
	}

	private void registerPrimaryDataSourceBeanDefinition( AcrossListableBeanFactory beanFactory, DataSource dataSource ) {
		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setPrimary( true );
		definition.setBeanClass( DataSource.class );
		beanFactory.registerBeanDefinition( DATASOURCE, definition );
		beanFactory.registerSingleton( DATASOURCE, dataSource );
	}

	private boolean hasMultipleDataSources( AcrossListableBeanFactory beanFactory ) {
		ListableBeanFactory parent = (ListableBeanFactory) beanFactory.getParentBeanFactory();

		Set<String> beanNames = new HashSet<>();
		while ( parent != null ) {
			beanNames.addAll( Arrays.asList( parent.getBeanNamesForType( DataSource.class ) ) );

			if ( parent instanceof HierarchicalBeanFactory ) {
				parent = (ListableBeanFactory) ( (HierarchicalBeanFactory) parent ).getParentBeanFactory();
			}
			else {
				parent = null;
			}
		}

		return beanNames.size() > 1;
	}

	private BeanDefinitionRegistry getRegistryWithLocalBeanDefinition( AcrossListableBeanFactory beanFactory, String beanName ) {
		BeanDefinitionRegistry bf = beanFactory;

		do {
			if ( bf.containsBeanDefinition( beanName ) ) {
				return bf;
			}

			if ( bf instanceof HierarchicalBeanFactory ) {
				bf = (BeanDefinitionRegistry) ( (HierarchicalBeanFactory) bf ).getParentBeanFactory();
			}
			else {
				bf = null;
			}
		}
		while ( bf != null );

		return null;
	}

	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
	}
}
