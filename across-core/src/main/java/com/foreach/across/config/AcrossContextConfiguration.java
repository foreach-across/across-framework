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
import com.foreach.across.core.support.AcrossContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.*;

/**
 * <p>Creates an AcrossContext bean and will apply all AcrossContextConfigurer instances
 * before bootstrapping.  Depending on the settings imported from {@link EnableAcrossContext},
 * modules will be auto-configured and further configuration of the context delegated to the configurer beans.</p>
 * <p>A DataSource bean named acrossDataSource is required for installers to work.</p>
 */
@Configuration
public class AcrossContextConfiguration implements ImportAware, EnvironmentAware
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContextConfiguration.class );

	static final String ANNOTATION_TYPE = EnableAcrossContext.class.getName();

	@Autowired(required = false)
	@Qualifier(AcrossContext.DATASOURCE)
	private DataSource dataSource;

	@Autowired(required = false)
	@Qualifier(AcrossContext.INSTALLER_DATASOURCE)
	private DataSource installerDataSource;

	@Autowired(required = false)
	private Collection<AcrossContextConfigurer> configurers = Collections.emptyList();

	@Autowired(required = false)
	private Collection<AcrossModule> moduleBeans = Collections.emptyList();

	private AnnotationMetadata importMetadata;
	private Environment environment;

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		this.importMetadata = importMetadata;
	}

	@Override
	public void setEnvironment( Environment environment ) {
		this.environment = environment;
	}

	@Bean
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
		Map<String, Object> configuration = importMetadata.getAnnotationAttributes( ANNOTATION_TYPE );

		AcrossContextBuilder contextBuilder = new AcrossContextBuilder()
				.applicationContext( applicationContext )
				.dataSource( dataSource )
				.installerDataSource( installerDataSource )
				.developmentMode( isDevelopmentMode() )
				.moduleConfigurationPackages( determineModuleConfigurationPackages( configuration ) )
				.configurer( configurers.toArray( new AcrossContextConfigurer[configurers.size()] ) );

		autoConfigureContextBuilder( contextBuilder, importMetadata.getAnnotationAttributes( ANNOTATION_TYPE ) );

		AcrossContext context = contextBuilder.build();
		context.bootstrap();

		return context;
	}

	private void autoConfigureContextBuilder( AcrossContextBuilder contextBuilder,
	                                          Map<String, Object> configuration ) {
		if ( configuration != null ) {
			if ( Boolean.TRUE.equals( configuration.get( "autoConfigure" ) ) ) {
				contextBuilder.moduleDependencyResolver( moduleDependencyResolver() )
				              .modules( namedModulesToConfigure( configuration ) )
				              .modules( moduleBeans.toArray( new AcrossModule[moduleBeans.size()] ) );
			}
		}
	}

	private boolean isDevelopmentMode() {
		if ( environment.containsProperty( "across.development.active" ) ) {
			return environment.getProperty( "across.development.active", Boolean.class, false );
		}
		else if ( environment.acceptsProfiles( "dev" ) ) {
			LOG.info( "Activating development mode for Across because of 'dev' Spring profile" );
			return true;
		}
		return false;
	}

	private String[] namedModulesToConfigure( Map<String, Object> configuration ) {
		String[] valueModuleNames = (String[]) configuration.get( "value" );
		String[] moduleNames = (String[]) configuration.get( "modules" );

		Set<String> modules = new LinkedHashSet<>();
		modules.addAll( Arrays.asList( valueModuleNames ) );
		modules.addAll( Arrays.asList( moduleNames ) );

		return modules.toArray( new String[modules.size()] );
	}

	@Bean
	@Lazy
	public ModuleDependencyResolver moduleDependencyResolver() {
		ClassPathScanningModuleDependencyResolver moduleDependencyResolver
				= new ClassPathScanningModuleDependencyResolver();

		Map<String, Object> configuration = importMetadata.getAnnotationAttributes( ANNOTATION_TYPE );
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
			modulePackageSet.add( AcrossContextBuilder.STANDARD_MODULES_PACKAGE );

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
}
