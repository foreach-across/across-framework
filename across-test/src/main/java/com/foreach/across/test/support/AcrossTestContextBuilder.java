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
package com.foreach.across.test.support;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.ExposedBeansBootstrapConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.configurer.PropertyPlaceholderSupportConfigurer;
import com.foreach.across.core.support.AcrossContextBuilder;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.support.config.AcrossContextBuilderConfigurerRegistrar;
import com.foreach.across.test.support.config.ResetDatabaseConfigurer;
import com.foreach.across.test.support.config.TestDataSourceConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.sql.DataSource;
import java.util.*;

/**
 * Builder for creating an {@link AcrossTestContext}.
 * This builder allows easy configuration of properties and modules to add to an {@link AcrossTestContext}.
 * <p>
 * Once {@link #build()} has been called, the {@link AcrossTestContext} will be created and the internal
 * {@link com.foreach.across.core.AcrossContext} bootstrapped.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class AcrossTestContextBuilder
{
	private int propertiesCounter = 0;

	private final Properties properties = new Properties();
	private final List<PropertySource<?>> propertySources = new ArrayList<>();
	private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();
	private final Set<Class<?>> exposeClasses = new LinkedHashSet<>();
	private final AcrossContextBuilder contextBuilder = new AcrossContextBuilder();

	public AcrossTestContextBuilder() {
		properties( properties );
		annotatedClasses.add( PropertyPlaceholderSupportConfigurer.Config.class );
		annotatedClasses.add( AcrossContextBuilderConfigurerRegistrar.class );
		useTestDataSource( true ).dropFirst( true );
	}

	/**
	 * Should the database be reset before bootstrapping the {@link AcrossContext}.
	 * When {@code true}, this adds the {@link ResetDatabaseConfigurer} to the parent {@link ApplicationContext}.
	 * This is the default case.
	 *
	 * @param dropFirst true if datasource tables should be dropped before starting the context
	 * @return self
	 * @see ResetDatabaseConfigurer
	 */
	public AcrossTestContextBuilder dropFirst( boolean dropFirst ) {
		if ( dropFirst ) {
			annotatedClasses.add( ResetDatabaseConfigurer.class );
		}
		else {
			annotatedClasses.remove( ResetDatabaseConfigurer.class );
		}
		return this;
	}

	/**
	 * Should default test datasources be created.  This will use the datasource specified by the property
	 * <strong>acrossTest.datasource</strong> or create a memory HSQLDB if none is specified.
	 * <p>
	 * Using test datasource is the default.  This is the equivalent to adding {@link TestDataSourceConfigurer}
	 * the the parent {@link ApplicationContext}.
	 * </p>
	 *
	 * @param useTestDataSource true if test datasource should be created automatically
	 * @return self
	 * @see TestDataSourceConfigurer
	 */
	public AcrossTestContextBuilder useTestDataSource( boolean useTestDataSource ) {
		if ( useTestDataSource ) {
			annotatedClasses.add( TestDataSourceConfigurer.class );
		}
		else {
			annotatedClasses.remove( TestDataSourceConfigurer.class );
		}
		return this;
	}

	/**
	 * Add one or more annotated classes to the parent {@link ApplicationContext} that is attached to the
	 * {@link AcrossContext}.
	 *
	 * @param annotatedClasses list
	 * @return self
	 */
	public AcrossTestContextBuilder register( Class<?>... annotatedClasses ) {
		Collections.addAll( this.annotatedClasses, annotatedClasses );
		return this;
	}

	/**
	 * Add one or more types (classes, interfaces, annotations) that should be exposed in this test context.
	 *
	 * @param classesToExpose list
	 * @return self
	 */
	public AcrossTestContextBuilder expose( Class<?>... classesToExpose ) {
		Collections.addAll( this.exposeClasses, classesToExpose );
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
	public AcrossTestContextBuilder dataSource( DataSource dataSource ) {
		contextBuilder.dataSource( dataSource );
		return this;
	}

	/**
	 * Set the installer datasource.
	 *
	 * @param installerDataSource instance
	 * @return self
	 */
	public AcrossTestContextBuilder installerDataSource( DataSource installerDataSource ) {
		contextBuilder.installerDataSource( installerDataSource );
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
	public AcrossTestContextBuilder modules( String... moduleNames ) {
		contextBuilder.modules( moduleNames );
		return this;
	}

	/**
	 * Array of {@link AcrossModule} instances to add to the context.  These will be added after the modules
	 * added using {@link #modules(String...)} have been resolved.  Subsequent calls will add modules.
	 *
	 * @param modules collection
	 * @return self
	 */
	public AcrossTestContextBuilder modules( AcrossModule... modules ) {
		contextBuilder.modules( modules );
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
	public AcrossTestContextBuilder moduleDependencyResolver( ModuleDependencyResolver moduleDependencyResolver ) {
		contextBuilder.moduleDependencyResolver( moduleDependencyResolver );
		return this;
	}

	/**
	 * Additional packages that should be scanned for modules if no custom {@link ModuleDependencyResolver} is configured.
	 * In case of a custom resolver, this setting will be ignored.
	 * <p>
	 * The default {@link AcrossContextBuilder#STANDARD_MODULES_PACKAGE} will always be added as the first one.  If you do not want this,
	 * you should set a custom {@link ModuleDependencyResolver} using {@link #moduleDependencyResolver(ModuleDependencyResolver)}.
	 * </p>
	 * <p>Subsequent calls will add additional packages.</p>
	 *
	 * @param packageNames list
	 * @return self
	 * @see #moduleDependencyResolver(ModuleDependencyResolver)
	 */
	public AcrossTestContextBuilder additionalModulePackages( String... packageNames ) {
		contextBuilder.additionalModulePackages( packageNames );
		return this;
	}

	/**
	 * Type-safe alternative for {@link #additionalModulePackages(String...)} where the packages of the classes
	 * will be added for module scanning.  In case of a custom resolver, this setting will be ignored.
	 * <p>
	 * The default {@link AcrossContextBuilder#STANDARD_MODULES_PACKAGE} will always be added as the first one.  If you do not want this,
	 * you should set a custom {@link ModuleDependencyResolver} using {@link #moduleDependencyResolver(ModuleDependencyResolver)}.
	 * </p>
	 * <p>Subsequent calls will add additional packages.</p>
	 *
	 * @param classes list
	 * @return self
	 */
	public AcrossTestContextBuilder additionalModulePackageClasses( Class<?>... classes ) {
		contextBuilder.additionalModulePackageClasses( classes );
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
	public AcrossTestContextBuilder moduleConfigurationPackages( String... packageNames ) {
		contextBuilder.moduleConfigurationPackages( packageNames );
		return this;
	}

	/**
	 * Type-safe alternative for {@link #moduleConfigurationPackages(String...)}.
	 *
	 * @param classes list
	 * @return self
	 */
	public AcrossTestContextBuilder moduleConfigurationPackageClasses( Class<?>... classes ) {
		contextBuilder.moduleConfigurationPackageClasses( classes );
		return this;
	}

	/**
	 * @param developmentMode true if development mode should be active on the created context
	 * @return self
	 */
	public AcrossTestContextBuilder developmentMode( boolean developmentMode ) {
		contextBuilder.developmentMode( developmentMode );
		return this;
	}

	/**
	 * Add one or more {@link AcrossContextConfigurer} instances that are to be called after the initial
	 * {@link AcrossContext} has been created.  Subsequent calls will add additional configurers.
	 *
	 * @param configurer one or more instances
	 * @return self
	 */
	public AcrossTestContextBuilder configurer( AcrossContextConfigurer... configurer ) {
		contextBuilder.configurer( configurer );
		return this;
	}

	/**
	 * Set a property value on the environment of the parent {@link ApplicationContext}.
	 *
	 * @param key   property key
	 * @param value property value
	 * @return self
	 */
	public AcrossTestContextBuilder property( String key, Object value ) {
		properties.put( key, value );
		return this;
	}

	/**
	 * Add a properties collection to the environment of the parent {@link ApplicationContext}.
	 *
	 * @param properties instance
	 * @return self
	 */
	public AcrossTestContextBuilder properties( Properties properties ) {
		properties( new PropertiesPropertySource( "AcrossTestContextBuilder" + propertiesCounter++, properties ) );
		return this;
	}

	/**
	 * Add a properties map to the environment of the parent {@link ApplicationContext}.
	 *
	 * @param properties map
	 * @return self
	 */
	public AcrossTestContextBuilder properties( Map<String, Object> properties ) {
		properties( new MapPropertySource( "AcrossTestContextBuilder:" + propertiesCounter++, properties ) );
		return this;
	}

	/**
	 * Add a {@link PropertySource} to the environment of the parent {@link ApplicationContext}.
	 *
	 * @param propertySource instance
	 * @return self
	 */
	public AcrossTestContextBuilder properties( PropertySource propertySource ) {
		propertySources.add( propertySource );
		return this;
	}

	/**
	 * Creates the internal {@link AcrossContext} and bootstraps it.  Then wraps it in a {@link AcrossTestContext}
	 * instance that gives easy access to querying functionality.
	 *
	 * @return queryable context containing the bootstrapped {@link AcrossContext}
	 */
	public AcrossTestContext build() {
		AcrossConfigurableApplicationContext applicationContext = createDefaultApplicationContext();
		configureApplicationContext( applicationContext );

		ProvidedBeansMap beans = new ProvidedBeansMap();
		beans.put( "contextBuilder", contextBuilder.applicationContext( applicationContext ) );

		if ( !exposeClasses.isEmpty() ) {
			beans.put( "exposedTestClassesConfiguration", new ExposedBeansBootstrapConfigurer( exposeClasses ) );
		}

		applicationContext.provide( beans );

		applicationContext.refresh();
		applicationContext.start();

		AcrossContext context = contextBuilder.build();
		context.bootstrap();

		return createTestContext( applicationContext, context );
	}

	protected AcrossTestContext createTestContext( AcrossConfigurableApplicationContext applicationContext,
	                                               AcrossContext context ) {
		return new RunningAcrossContext( applicationContext, context );
	}

	protected AcrossConfigurableApplicationContext createDefaultApplicationContext() {
		return new AcrossApplicationContext();
	}

	protected void configureApplicationContext( AcrossConfigurableApplicationContext applicationContext ) {
		Class<?>[] classesToRegister = annotatedClasses();
		if ( classesToRegister.length > 0 ) {
			applicationContext.register( classesToRegister );
		}

		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		propertySources.forEach( environment.getPropertySources()::addLast );
	}

	protected Class<?>[] annotatedClasses() {
		return annotatedClasses.toArray( new Class<?>[annotatedClasses.size()] );
	}

	private static class RunningAcrossContext extends AcrossTestContext
	{
		RunningAcrossContext( ConfigurableApplicationContext parentApplicationContext,
		                      AcrossContext acrossContext ) {
			setApplicationContext( parentApplicationContext );
			setAcrossContext( acrossContext );
		}
	}
}
