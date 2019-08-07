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
package com.foreach.across.core.support;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.ClassPathScanningCandidateModuleProvider;
import com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.support.ModuleSetBuilder;
import com.foreach.across.core.installers.InstallerAction;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Convenience builder for an {@link AcrossContext}.  Provides a chaining builder for most common properties
 * when setting up an {@link AcrossContext}.  Complex configuration changes can easily be done by adding
 * {@link AcrossContextConfigurer} instances using {@link #configurer(AcrossContextConfigurer...)}.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.config.AcrossContextConfiguration
 * @since 1.1.2
 */
public class AcrossContextBuilder
{
	public static final String STANDARD_MODULES_PACKAGE = "com.foreach.across.modules";

	private final Set<String> moduleNames = new LinkedHashSet<>();
	private final Set<AcrossModule> modules = new LinkedHashSet<>();
	private final Set<String> moduleScanPackages = new LinkedHashSet<>();
	private final Set<String> moduleConfigurationScanPackages = new LinkedHashSet<>();
	private final Set<AcrossContextConfigurer> contextConfigurers = new LinkedHashSet<>();

	private boolean developmentMode;
	private ModuleDependencyResolver moduleDependencyResolver;
	private DataSource dataSource, installerDataSource;
	private ApplicationContext applicationContext;

	public AcrossContextBuilder() {
		moduleScanPackages.add( STANDARD_MODULES_PACKAGE );
	}

	/**
	 * Set the parent {@link ApplicationContext} to be attached to the {@link AcrossContext}.
	 *
	 * @param applicationContext instance
	 * @return self
	 */
	public AcrossContextBuilder applicationContext( ApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
		return this;
	}

	/**
	 * Set the core across datasource.  If no separate {@link #installerDataSource(DataSource)} is configured,
	 * this datasource will also be used for installers.  If neither datasource is configured, installers will be
	 * disabled.
	 *
	 * @param dataSource instance
	 * @return self
	 */
	public AcrossContextBuilder dataSource( DataSource dataSource ) {
		this.dataSource = dataSource;
		return this;
	}

	/**
	 * Set the installer datasource.
	 *
	 * @param installerDataSource instance
	 * @return self
	 */
	public AcrossContextBuilder installerDataSource( DataSource installerDataSource ) {
		this.installerDataSource = installerDataSource;
		return this;
	}

	/**
	 * Array of {@link AcrossModule} names that should be configured.
	 * These will be resolved using the configured {@link #moduleDependencyResolver(ModuleDependencyResolver)},
	 * before any configured module instances and before the {@link AcrossContextConfigurer} instances are called.
	 * If no {@link ModuleDependencyResolver} was set explicitly, the default will be used.
	 * <p>
	 * Subsequent calls will add modules.
	 * </p>
	 *
	 * @param moduleNames names
	 * @return self
	 */
	public AcrossContextBuilder modules( String... moduleNames ) {
		Collections.addAll( this.moduleNames, moduleNames );
		return this;
	}

	/**
	 * Array of {@link AcrossModule} instances to add to the context.  These will be added after the modules
	 * added using {@link #modules(String...)} have been resolved.  Subsequent calls will add modules.
	 *
	 * @param modules collection
	 * @return self
	 */
	public AcrossContextBuilder modules( AcrossModule... modules ) {
		Collections.addAll( this.modules, modules );
		return this;
	}

	/**
	 * Set the {@link ModuleDependencyResolver} that should be used for looking up modules by name.
	 * If none is set, a default {@link com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver}
	 * will be used.
	 *
	 * @param moduleDependencyResolver instance
	 * @return self
	 * @see com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver
	 */
	public AcrossContextBuilder moduleDependencyResolver( ModuleDependencyResolver moduleDependencyResolver ) {
		this.moduleDependencyResolver = moduleDependencyResolver;
		return this;
	}

	/**
	 * Additional packages that should be scanned for modules if no custom {@link ModuleDependencyResolver} is configured.
	 * In case of a custom resolver, this setting will be ignored.
	 * <p>
	 * The default {@link #STANDARD_MODULES_PACKAGE} will always be added as the first one.  If you do not want this,
	 * you should set a custom {@link ModuleDependencyResolver} using {@link #moduleDependencyResolver(ModuleDependencyResolver)}.
	 * </p>
	 * <p>Subsequent calls will add additional packages.</p>
	 *
	 * @param packageNames list
	 * @return self
	 * @see #moduleDependencyResolver(ModuleDependencyResolver)
	 */
	public AcrossContextBuilder additionalModulePackages( String... packageNames ) {
		Collections.addAll( moduleScanPackages, packageNames );
		return this;
	}

	/**
	 * Type-safe alternative for {@link #additionalModulePackages(String...)} where the packages of the classes
	 * will be added for module scanning.  In case of a custom resolver, this setting will be ignored.
	 * <p>
	 * The default {@link #STANDARD_MODULES_PACKAGE} will always be added as the first one.  If you do not want this,
	 * you should set a custom {@link ModuleDependencyResolver} using {@link #moduleDependencyResolver(ModuleDependencyResolver)}.
	 * </p>
	 * <p>Subsequent calls will add additional packages.</p>
	 *
	 * @param classes list
	 * @return self
	 */
	public AcrossContextBuilder additionalModulePackageClasses( Class<?>... classes ) {
		for ( Class<?> clazz : classes ) {
			moduleScanPackages.add( clazz.getPackage().getName() );
		}
		return this;
	}

	/**
	 * Add one or more custom packages that should be scanned for
	 * {@link com.foreach.across.core.annotations.ModuleConfiguration} classes.
	 * Subsequent calls will add additional packages.
	 *
	 * @param packageNames list
	 * @return self
	 */
	public AcrossContextBuilder moduleConfigurationPackages( String... packageNames ) {
		Collections.addAll( moduleConfigurationScanPackages, packageNames );
		return this;
	}

	/**
	 * Type-safe alternative for {@link #moduleConfigurationPackages(String...)}.
	 *
	 * @param classes list
	 * @return self
	 */
	public AcrossContextBuilder moduleConfigurationPackageClasses( Class<?>... classes ) {
		for ( Class<?> clazz : classes ) {
			moduleConfigurationScanPackages.add( clazz.getPackage().getName() );
		}
		return this;
	}

	/**
	 * @param developmentMode true if development mode should be active on the created context
	 * @return self
	 */
	public AcrossContextBuilder developmentMode( boolean developmentMode ) {
		this.developmentMode = developmentMode;
		return this;
	}

	/**
	 * Add one or more {@link AcrossContextConfigurer} instances that are to be called after the initial
	 * {@link AcrossContext} has been created.  Subsequent calls will add additional configurers.
	 *
	 * @param configurer one or more instances
	 * @return self
	 */
	public AcrossContextBuilder configurer( AcrossContextConfigurer... configurer ) {
		Collections.addAll( contextConfigurers, configurer );
		return this;
	}

	/**
	 * Assembles the actual {@link AcrossContext} instance and returns it.
	 * This does not bootstrap the context created.
	 *
	 * @return instance
	 */
	public AcrossContext build() {
		AcrossContext context = new AcrossContext();
		context.setParentApplicationContext( applicationContext );
		context.setDevelopmentMode( developmentMode );
		context.setDataSource( dataSource() );

		DataSource installerDataSource = installerDataSource();
		context.setInstallerDataSource( installerDataSource );
		context.setInstallerAction( installerDataSource != null ? InstallerAction.EXECUTE : InstallerAction.DISABLED );

		ModuleDependencyResolver dependencyResolver = moduleDependencyResolver( applicationContext );
		context.setModuleDependencyResolver( dependencyResolver );

		configureModules( context, dependencyResolver );

		context.setModuleConfigurationScanPackages(
				moduleConfigurationScanPackages.toArray( new String[0] )
		);

		executeConfigurers( context );

		return context;
	}

	protected ModuleDependencyResolver moduleDependencyResolver( ApplicationContext applicationContext ) {
		if ( moduleDependencyResolver == null ) {
			ClassPathScanningModuleDependencyResolver dependencyResolver = new ClassPathScanningModuleDependencyResolver(
					new ClassPathScanningCandidateModuleProvider(
							applicationContext != null ? applicationContext : new PathMatchingResourcePatternResolver()
					)
			);
			dependencyResolver.setBasePackages( moduleScanPackages.toArray( new String[0] ) );

			return dependencyResolver;
		}

		return moduleDependencyResolver;
	}

	protected void executeConfigurers( AcrossContext context ) {
		contextConfigurers.forEach( c -> c.configure( context ) );
	}

	protected DataSource installerDataSource() {
		if ( installerDataSource != null ) {
			return installerDataSource;
		}

		return dataSource();
	}

	protected DataSource dataSource() {
		return dataSource;
	}

	protected void configureModules( AcrossContext context, ModuleDependencyResolver dependencyResolver ) {
		ModuleSetBuilder moduleSetBuilder = new ModuleSetBuilder();
		moduleSetBuilder.setDependencyResolver( dependencyResolver );

		moduleNames.forEach( moduleSetBuilder::addModule );
		modules.forEach( moduleSetBuilder::addModule );
		context.setModules( moduleSetBuilder.build().getModules() );
	}
}
