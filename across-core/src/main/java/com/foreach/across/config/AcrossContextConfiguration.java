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
package com.foreach.across.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.support.ModuleSetBuilder;
import com.foreach.across.core.installers.InstallerAction;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.*;

/**
 * <p>Creates an AcrossContext bean and will apply all AcrossContextConfigurer instances
 * before bootstrapping.  Depending on the settings imported from {@link EnableAcrossContext},
 * modules will be auto-configured and further configuration of the context delegated to the configurer beans.</p>
 * <p>A DataSource bean names acrossDataSource is required.</p>
 */
@Configuration
public class AcrossContextConfiguration implements ImportAware
{
	public static final String STANDARD_MODULES_PACKAGE = "com.foreach.across.modules";

	static final String ANNOTATION_TYPE = EnableAcrossContext.class.getName();

	@Autowired(required = false)
	@Qualifier(AcrossContext.DATASOURCE)
	private DataSource dataSource;

	@Autowired(required = false)
	@Qualifier(AcrossContext.INSTALLER_DATASOURCE)
	private DataSource installerDataSource;

	@Autowired(required = false)
	private Collection<AcrossContextConfigurer> configurers = Collections.emptyList();

	private AnnotationMetadata importMetadata;

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		this.importMetadata = importMetadata;
	}

	@SuppressWarnings("all")
	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
		AcrossContext context
				= createAcrossContext( applicationContext, importMetadata.getAnnotationAttributes( ANNOTATION_TYPE ) );

		// Set installer datasource
		DataSource ds = dataSourceForInstallers();

		if ( ds != null ) {
			context.setInstallerAction( InstallerAction.EXECUTE );
			context.setInstallerDataSource( ds );
		}
		else {
			context.setInstallerAction( InstallerAction.DISABLED );
			System.err.println(
					"No datasource bean named acrossDataSource or acrossInstallerDataSource found - " +
							"configuring a context without datasource and disabling the installers." );
		}

		// Set the context datasource
		context.setDataSource( dataSource );

		for ( AcrossContextConfigurer configurer : configurers ) {
			configurer.configure( context );
		}

		// Start the context
		context.bootstrap();

		return context;
	}

	private AcrossContext createAcrossContext( ConfigurableApplicationContext applicationContext,
	                                           Map<String, Object> configuration ) {
		AcrossContext acrossContext = new AcrossContext( applicationContext );

		if ( configuration != null ) {
			if ( Boolean.TRUE.equals( configuration.get( "autoConfigure" ) ) ) {
				ModuleSetBuilder moduleSetBuilder = new ModuleSetBuilder();

				ModuleDependencyResolver dependencyResolver = moduleDependencyResolver( configuration );
				moduleSetBuilder.setDependencyResolver( dependencyResolver );
				acrossContext.setModuleDependencyResolver( dependencyResolver );

				modulesToConfigure( configuration ).forEach( moduleSetBuilder::addModule );

				BeanFactoryUtils
						.beansOfTypeIncludingAncestors( applicationContext, AcrossModule.class )
						.values()
						.forEach( moduleSetBuilder::addModule );

				acrossContext.setModules( moduleSetBuilder.build().getModules() );

				acrossContext.setModuleConfigurationScanPackages(
						determineModuleConfigurationPackages( configuration )
				);
			}
		}

		return acrossContext;
	}

	private Collection<String> modulesToConfigure( Map<String, Object> configuration ) {
		String[] valueModuleNames = (String[]) configuration.get( "value" );
		String[] moduleNames = (String[]) configuration.get( "modules" );

		Set<String> modules = new LinkedHashSet<>();
		modules.addAll( Arrays.asList( valueModuleNames ) );
		modules.addAll( Arrays.asList( moduleNames ) );

		return modules;
	}

	private ModuleDependencyResolver moduleDependencyResolver( Map<String, Object> configuration ) {
		ClassPathScanningModuleDependencyResolver moduleDependencyResolver
				= new ClassPathScanningModuleDependencyResolver();

		if ( configuration != null ) {
			moduleDependencyResolver.setResolveRequired(
					Boolean.TRUE.equals( configuration.get( "scanForRequiredModules" ) )
			);
			moduleDependencyResolver.setResolveOptional(
					Boolean.TRUE.equals( configuration.get( "scanForOptionalModules" ) )
			);
			moduleDependencyResolver.setExcludedModules(
					Arrays.asList( (String[]) configuration.get( "excludeFromScanning" ) )
			);
			moduleDependencyResolver.setBasePackages( determineModulePackages( configuration ) );
		}

		return moduleDependencyResolver;
	}

	private String[] determineModuleConfigurationPackages( Map<String, Object> configuration ) {
		Set<String> configurationPackageSet = new LinkedHashSet<>();

		String[] configurationPackages = (String[]) configuration.get( "moduleConfigurationPackages" );
		Collections.addAll( configurationPackageSet, configurationPackages );

		Class<?>[] configurationPackageClasses = (Class<?>[]) configuration.get( "moduleConfigurationPackageClasses" );

		for ( Class<?> modulePackageClass : configurationPackageClasses ) {
			configurationPackageSet.add( modulePackageClass.getPackage().getName() );
		}

		if ( configurationPackageSet.isEmpty() ) {
			try {
				Class<?> importingClass = Class.forName( importMetadata.getClassName() );
				Package importingClassPackage = importingClass.getPackage();

				String base = "";

				if ( importingClassPackage != null ) {
					base = importingClass.getPackage().getName();
				}

				configurationPackageSet.add( base + ".config" );
				configurationPackageSet.add( base + ".extensions" );
			}
			catch ( ClassNotFoundException ignore ) {
			}
		}

		return configurationPackageSet.toArray( new String[configurationPackageSet.size()] );
	}

	private String[] determineModulePackages( Map<String, Object> configuration ) {
		Set<String> modulePackageSet = new LinkedHashSet<>();

		String[] modulePackages = (String[]) configuration.get( "modulePackages" );
		Collections.addAll( modulePackageSet, modulePackages );

		Class<?>[] modulePackageClasses = (Class<?>[]) configuration.get( "modulePackageClasses" );

		for ( Class<?> modulePackageClass : modulePackageClasses ) {
			modulePackageSet.add( modulePackageClass.getPackage().getName() );
		}

		if ( modulePackageSet.isEmpty() ) {
			modulePackageSet.add( STANDARD_MODULES_PACKAGE );

			try {
				Class<?> importingClass = Class.forName( importMetadata.getClassName() );
				Package importingClassPackage = importingClass.getPackage();

				if ( importingClassPackage != null ) {
					modulePackageSet.add( importingClass.getPackage().getName() );
				}
				else {
					modulePackageSet.add( "modules" );
				}
			}
			catch ( ClassNotFoundException ignore ) {
			}
		}

		return modulePackageSet.toArray( new String[modulePackageSet.size()] );
	}

	private DataSource dataSourceForInstallers() {
		if ( installerDataSource != null ) {
			return installerDataSource;
		}

		return dataSource;
	}
}
