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

package com.foreach.across.core;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.core.context.AbstractAcrossEntity;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.PropertySourcesConfigurer;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class representing a set of Across modules.
 * This class takes care of managing the global Spring ApplicationContext related to all modules,
 * and will make sure that different modules are handled in the order in which they are registered.
 */
public class AcrossContext extends AbstractAcrossEntity implements DisposableBean
{
	public static final String BEAN = "acrossContext";
	public static final String DATASOURCE = "acrossDataSource";
	public static final String INSTALLER_DATASOURCE = "acrossInstallerDataSource";
	private static final AtomicInteger ID_GENERATOR = new AtomicInteger( 1 );
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContext.class );
	private final String id;
	private DataSource dataSource;
	private DataSource installerDataSource;
	// By default no installers are allowed
	private InstallerSettings installerSettings = new InstallerSettings( InstallerAction.DISABLED );
	private Map<ApplicationContextConfigurer, ConfigurerScope> applicationContextConfigurers =
			new LinkedHashMap<>();
	private List<ApplicationContextConfigurer> installerContextConfigurers = new ArrayList<>();
	private Map<String, AcrossModule> modules = new LinkedHashMap<>();
	private boolean developmentMode;
	private boolean disableNoOpCacheManager = false;
	private boolean isBootstrapped = false;
	private ApplicationContext parentApplicationContext;

	private ExposedBeanDefinitionTransformer exposeTransformer = null;
	private ModuleDependencyResolver moduleDependencyResolver = null;

	private String[] moduleConfigurationScanPackages = new String[0];

	private String displayName;

	/**
	 * Constructs a new AcrossContext in its own ApplicationContext.
	 * Modules in this context will not be able to use any beans defined outside the AcrossContext.
	 */
	public AcrossContext() {
		this( null );
	}

	/**
	 * Constructs a new AcrossContext that is a child of the parent ApplicationContext passed in.
	 * Modules can access all beans from the parent context, and exposed beans will be copied to the parent
	 * context so non-Across beans can access them.
	 *
	 * @param parentContext Parent ApplicationContext.
	 */
	public AcrossContext( ApplicationContext parentContext ) {
		id = "AcrossContext-" + ID_GENERATOR.getAndIncrement();
		parentApplicationContext = parentContext;
		displayName = id;
	}

	/**
	 * The unique id of this AcrossContext.  Used in generating BeanDefinition names.
	 *
	 * @return The unique id of this AcrossContext.
	 */
	public String getId() {
		return id;
	}

	@Override
	public AcrossVersionInfo getVersionInfo() {
		return AcrossVersionInfo.load( AcrossContext.class );
	}

	public ApplicationContext getParentApplicationContext() {
		return parentApplicationContext;
	}

	public void setParentApplicationContext( ApplicationContext parentApplicationContext ) {
		this.parentApplicationContext = parentApplicationContext;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	public DataSource getInstallerDataSource() {
		return installerDataSource;
	}

	public void setInstallerDataSource( DataSource installerDataSource ) {
		this.installerDataSource = installerDataSource;
	}

	public Collection<AcrossModule> getModules() {
		return modules.values();
	}

	public void setModules( Collection<AcrossModule> modules ) {
		this.modules.clear();
		modules.forEach( this::addModule );
	}

	public boolean isDevelopmentMode() {
		return developmentMode;
	}

	public void setDevelopmentMode( boolean developmentMode ) {
		this.developmentMode = developmentMode;
	}

	/**
	 * @return display name for the context (usually application name or something like that)
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the display name for this Across context.
	 *
	 * @param displayName for the context
	 */
	public void setDisplayName( String displayName ) {
		this.displayName = StringUtils.defaultString( displayName, id );
	}

	/**
	 * Specifies whether we want to fall back on a {@link org.springframework.cache.support.NoOpCacheManager NoOpCacheManager}
	 * when asking the global cache manager for a specific cache instance.
	 * Leaving this enabled potentially avoids many null checks when dealing with the caching layer.
	 * It does open up the potential for misconfigured caches, as you might inadvertently omit configurations.
	 *
	 * @return <code>true</code> if we do not want to fall back on a NoOpCacheManager
	 * @see com.foreach.across.core.cache.AcrossCompositeCacheManager
	 */
	public boolean isDisableNoOpCacheManager() {
		return disableNoOpCacheManager;
	}

	public void setDisableNoOpCacheManager( boolean disableNoOpCacheManager ) {
		this.disableNoOpCacheManager = disableNoOpCacheManager;
	}

	public void addModule( @NonNull AcrossModule module ) {
		Assert.notNull( module.getName(), "An AcrossModule must have a valid unique name." );

		if ( module.getContext() != null ) {
			throw new AcrossException( "Module is already attached to another AcrossContext: " + module );
		}

		if ( isBootstrapped ) {
			throw new AcrossException(
					"Adding a module to an already bootstrapped AcrossContext is currently not supported." );
		}

		modules.put( module.getName(), module );
		module.setContext( this );
	}

	/**
	 * Gets the module with the given name if present on the context.
	 *
	 * @param name Name of the module.
	 * @return AcrossModule or {@code null} if not present.
	 */
	public AcrossModule getModule( String name ) {
		for ( AcrossModule module : modules.values() ) {
			if ( StringUtils.equals( module.getName(), name ) ) {
				return module;
			}
		}

		return null;
	}

	/**
	 * Gets the module with the given name if present on the context.  Requires the module to be of the specific type,
	 * if this is not the case a {@link ClassCastException} will be thrown.
	 *
	 * @param name       Name of the module
	 * @param moduleType Required module type
	 * @return AcrossModule or {@code null} if not present
	 */
	public <U extends AcrossModule> U getModule( String name, Class<U> moduleType ) {
		for ( AcrossModule module : modules.values() ) {
			if ( StringUtils.equals( module.getName(), name ) ) {
				return moduleType.cast( module );
			}
		}

		return null;
	}

	/**
	 * Gets the InstallerSettings attached to this context.  A context should always have specific
	 * InstallerSettings, so this method can never return null.
	 *
	 * @return InstallerSettings instance.
	 */
	public InstallerSettings getInstallerSettings() {
		return installerSettings;
	}

	/**
	 * Sets the InstallerSettings for this context.
	 *
	 * @param installerSettings InstallerSettings instance.
	 */
	public void setInstallerSettings( InstallerSettings installerSettings ) {
		Assert.notNull( installerSettings, "InstallerSettings on AcrossContext may not be null." );
		this.installerSettings = installerSettings;
	}

	/**
	 * Shortcut method that returns the default action on the InstallerSettings attached to the context.
	 *
	 * @return InstallerAction that is the default for the entire context.
	 */
	public InstallerAction getInstallerAction() {
		return installerSettings.getDefaultAction();
	}

	/**
	 * Shortcut method to set the default action on the InstallerSettings attached to the context.
	 *
	 * @param defaultAction InstallerAction to set as default.
	 */
	public void setInstallerAction( InstallerAction defaultAction ) {
		installerSettings.setDefaultAction( defaultAction );
	}

	public Map<ApplicationContextConfigurer, ConfigurerScope> getApplicationContextConfigurers() {
		return applicationContextConfigurers;
	}

	/**
	 * <p>Add an ApplicationContextConfigurer to the AcrossContext.  Depending on the scope the configurer
	 * will be applied to the root ApplicationContext only or to every ApplicationContext of every module registered.</p>
	 * <p>The latter is required for configurers providing BeanFactoryPostProcessor beans like property sources.</p>
	 *
	 * @param configurer Configurer instance.
	 * @param scope      Scope to which this configurer should be applied.
	 * @see com.foreach.across.core.context.configurer.ConfigurerScope
	 */
	public void addApplicationContextConfigurer( ApplicationContextConfigurer configurer, ConfigurerScope scope ) {
		applicationContextConfigurers.put( configurer, scope );
	}

	/**
	 * <p>Add an ApplicationContextConfigurer to the installer context of every module.</p>
	 *
	 * @param configurer Configurer instance.
	 * @see com.foreach.across.core.context.configurer.ConfigurerScope
	 */
	public void addInstallerContextConfigurer( ApplicationContextConfigurer configurer ) {
		installerContextConfigurers.add( configurer );
	}

	public List<ApplicationContextConfigurer> getInstallerContextConfigurers() {
		return installerContextConfigurers;
	}

	/**
	 * @return The transformer that will be applied to all exposed beans before copying them to the parent context.
	 */
	public ExposedBeanDefinitionTransformer getExposeTransformer() {
		return exposeTransformer;
	}

	/**
	 * Sets the transformer that will be applied to all exposed beans before actually copying them
	 * to the parent context.
	 *
	 * @param exposeTransformer The transformer that should be applies to all exposed beans.
	 */
	public void setExposeTransformer( ExposedBeanDefinitionTransformer exposeTransformer ) {
		this.exposeTransformer = exposeTransformer;
	}

	/**
	 * @return The instance that is being used for resolving the module dependencies.
	 */
	public ModuleDependencyResolver getModuleDependencyResolver() {
		return moduleDependencyResolver;
	}

	/**
	 * Sets the instance to be used for resolving the module dependencies.  If none is configured all
	 * modules will have to be added explicitly.
	 *
	 * @param moduleDependencyResolver instance
	 */
	public void setModuleDependencyResolver( ModuleDependencyResolver moduleDependencyResolver ) {
		this.moduleDependencyResolver = moduleDependencyResolver;
	}

	/**
	 * @return Packages that will be scanned for {@link ModuleConfiguration} classes
	 */
	public String[] getModuleConfigurationScanPackages() {
		return moduleConfigurationScanPackages.clone();
	}

	/**
	 * @param moduleConfigurationScanPackages packages that should be scanned for {@link ModuleConfiguration} classes
	 */
	public void setModuleConfigurationScanPackages( String... moduleConfigurationScanPackages ) {
		this.moduleConfigurationScanPackages = moduleConfigurationScanPackages;
	}

	/**
	 * Add PropertySources to the context.
	 *
	 * @param propertySources A PropertySources instance.
	 */
	@Override
	public void addPropertySources( PropertySources propertySources ) {
		// Only added to the context as they are merged in the environment of the module anyway
		addApplicationContextConfigurer( new PropertySourcesConfigurer( propertySources ),
		                                 ConfigurerScope.CONTEXT_ONLY );
	}

	/**
	 * Shortcut to add PropertySources to the context.
	 *
	 * @param propertySources One or more PropertySource instances.
	 */
	@Override
	public void addPropertySources( PropertySource<?>... propertySources ) {
		// Only added to the context as they are merged in the environment of the module anyway
		addApplicationContextConfigurer( new PropertySourcesConfigurer( propertySources ),
		                                 ConfigurerScope.CONTEXT_ONLY );
	}

	/**
	 * Shortcut method to publish an event synchronously on the AcrossContext event bus.
	 * For more fine-grained functionality like asynchronous publishing, use the AcrossEventPublisher.
	 *
	 * @param event Event instance that will be published.
	 */
	public void publishEvent( Object event ) {
		AcrossContextUtils.getApplicationContext( this ).publishEvent( event );
	}

	public void bootstrap() {
		if ( !isBootstrapped ) {
			isBootstrapped = true;

			try {
				new AcrossBootstrapper( this ).bootstrap();
			}
			catch ( Exception ignore ) {
				isBootstrapped = false;
				throw ignore;
			}
		}
	}

	public void shutdown() {
		if ( isBootstrapped ) {
			LOG.info( "Shutdown signal received - destroying ApplicationContext instances" );

			// Shutdown all modules in reverse order - note that it is quite possible that beans might have been destroyed
			// already by Spring in the meantime
			List<AcrossModuleInfo> reverseList = new ArrayList<>( AcrossContextUtils.getContextInfo( this ).getModules() );
			Collections.reverse( reverseList );

			for ( AcrossModuleInfo moduleInfo : reverseList ) {
				if ( moduleInfo.isBootstrapped() ) {
					AcrossModule module = moduleInfo.getModule();
					AcrossConfigurableApplicationContext applicationContext
							= AcrossContextUtils.getApplicationContext( module );

					if ( applicationContext != null ) {
						LOG.debug( "Destroying ApplicationContext for module {}", module.getName() );

						module.shutdown();
						applicationContext.destroy();
					}
				}
			}

			// Destroy the root ApplicationContext
			AcrossContextUtils.getApplicationContext( this ).destroy();

			LOG.debug( "Destroyed root ApplicationContext: {}", getId() );

			isBootstrapped = false;
		}
	}

	public boolean isBootstrapped() {
		return isBootstrapped;
	}

	public void destroy() {
		shutdown();
	}
}
