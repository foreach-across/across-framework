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

import com.foreach.across.core.AcrossException;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.BootstrapApplicationContextFactory;
import com.foreach.across.core.context.bootstrap.BootstrapLockManager;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the list of all installers in a configured AcrossContext.
 * Provides easy methods to run installers in different bootstrap phases.
 */
public class AcrossBootstrapInstallerRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossBootstrapInstallerRegistry.class );

	private final BootstrapLockManager bootstrapLockManager;
	private final AcrossBootstrapConfig contextConfig;
	private final BootstrapApplicationContextFactory applicationContextFactory;

	private final Map<AcrossModule, AcrossConfigurableApplicationContext> installerContexts = new HashMap<>();

	private AcrossInstallerRepository installerRepository;

	public AcrossBootstrapInstallerRegistry( AcrossBootstrapConfig contextConfig,
	                                         BootstrapLockManager bootstrapLockManager,
	                                         BootstrapApplicationContextFactory applicationContextFactory ) {
		this.bootstrapLockManager = bootstrapLockManager;
		this.contextConfig = contextConfig;
		this.applicationContextFactory = applicationContextFactory;
	}

	/**
	 * Runs all installers for all modules in the context.
	 *
	 * @param phase Bootstrap phase for installers.
	 */
	public void runInstallers( InstallerPhase phase ) {
		for ( ModuleBootstrapConfig moduleConfig : contextConfig.getModules() ) {
			runInstallers( moduleConfig, phase );
		}
	}

	/**
	 * Runs all installers for the given module and bootstrap phase.
	 *
	 * @param moduleName Unique name of the module in the context.
	 * @param phase      Bootstrap phase for installers.
	 */
	public void runInstallersForModule( String moduleName, InstallerPhase phase ) {
		runInstallers( contextConfig.getModule( moduleName ), phase );
	}

	private void runInstallers( ModuleBootstrapConfig moduleConfig, InstallerPhase phase ) {
		LOG.trace( "Running {} installers for module {}", phase.name(), moduleConfig.getModuleName() );

		for ( Object installerOrClass : moduleConfig.getInstallers() ) {
			Assert.notNull( installerOrClass, "Installer (class) instance should never be null." );

			Class<?> installerClass = determineInstallerClass( installerOrClass );
			Optional<Object> installerInstance = determineInstallerInstance( installerOrClass );

			InstallerMetaData metadata = InstallerMetaData.forClass( installerClass );

			if ( metadata.getInstallerPhase() == phase ) {
				// Create installer instance if necessary
				InstallerAction action = determineInstallerAction( metadata, moduleConfig );
				LOG.trace( "Determined action {} for installer {}.", action, metadata.getName() );

				if ( shouldCheckRunCondition( action ) ) {
					if ( shouldPerformAction( action, moduleConfig.getModule(), metadata ) ) {
						performInstallerAction( action, moduleConfig.getModule(), metadata, installerInstance );
					}
					else {
						LOG.debug( "Skipping installer {} because action {} should not be performed.",
						           metadata.getName(), action );
					}
				}
				else {
					LOG.debug( "Skipping installer {} because action is {}", metadata.getName(), action );
				}
			}
			else {
				LOG.trace( "Ignoring installer {} because it is defined for phase {}", metadata.getName(),
				           metadata.getInstallerPhase().name() );
			}
		}

		LOG.trace( "Finished {} installers for module {}", phase.name(), moduleConfig.getModuleName() );
	}

	private void performInstallerAction( InstallerAction action,
	                                     AcrossModule module,
	                                     InstallerMetaData installerMetaData,
	                                     Optional<Object> installerInstance ) {
		AcrossInstallerRepository repository = getInstallerRepository();

		boolean installed = false;

		if ( action != InstallerAction.REGISTER ) {
			Optional<Object> installer
					= prepareInstaller( module, installerMetaData.getInstallerClass(), installerInstance );

			if ( installer.isPresent() ) {
				LOG.info( "Executing installer {} for module {}", installerMetaData.getName(), module.getName() );

				for ( Method method : installerMetaData.getInstallerMethods() ) {
					try {
						method.setAccessible( true );
						method.invoke( installer.get() );

						installed = true;
					}
					catch ( Exception e ) {
						throw new AcrossException( e );
					}
				}

				if ( !installed ) {
					LOG.warn( "No @InstallerMethod methods were found for {}", installerMetaData.getName() );
				}

				LOG.trace( "Finished execution of installer {} for module {}", installer.getClass(), module.getName() );
			}
			else {
				LOG.debug( "Skipping installer {} - instance could not be retrieved (dependencies not met)" );
			}
		}
		else {
			// Register the installer version
			LOG.info(
					"Only performing registration of installer {} - version {} for module {}",
					installerMetaData.getName(),
					installerMetaData.getVersion(),
					module.getName()
			);

			installed = true;
		}

		if ( installed ) {
			repository.setInstalled( module, installerMetaData );
		}
	}

	private Optional<Object> prepareInstaller( AcrossModule module,
	                                           Class<?> installerClass,
	                                           Optional<Object> installerInstance ) {
		AcrossConfigurableApplicationContext installerContext = getInstallerContext( module );

		if ( !installerInstance.isPresent() ) {
			installerContext.register( installerClass );

			try {
				return Optional.ofNullable( BeanFactoryUtils.beanOfType( installerContext, installerClass ) );
			}
			catch ( NoSuchBeanDefinitionException nsbe ) {
				return Optional.empty();
			}
		}
		else {
			// For compatibility reasons
			LOG.warn(
					"Installer {} was passed as an instance - this functionality will be removed in future releases." );

			Object installer = installerInstance.get();
			AutowireCapableBeanFactory beanFactory = installerContext.getAutowireCapableBeanFactory();

			beanFactory.autowireBeanProperties( installer, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
			beanFactory.initializeBean( installer, "" );

			return installerInstance;
		}
	}

	private boolean shouldCheckRunCondition( InstallerAction action ) {
		return action != InstallerAction.DISABLED && action != InstallerAction.SKIP;
	}

	private boolean shouldPerformAction( InstallerAction action,
	                                     AcrossModule module,
	                                     InstallerMetaData installerMetaData ) {
		// Get the installer repository because now we need to perform version lookups
		// and possibly register on execution.  This will also install the core schema if necessary.
		AcrossInstallerRepository repository = getInstallerRepository();

		if ( action == InstallerAction.FORCE ) {
			return true;
		}

		switch ( installerMetaData.getRunCondition() ) {
			case AlwaysRun:
				LOG.debug( "Performing action {} for installer {} because it is set to always run", action,
				           installerMetaData.getInstallerClass() );
				return true;
			case VersionDifferent:
				int installedVersion = repository.getInstalledVersion( module, installerMetaData );
				if ( installerMetaData.getVersion() > installedVersion ) {
					LOG.debug( "Performing action {} for installer {} because version {} is higher than installed {}",
					           action, installerMetaData.getInstallerClass(), installerMetaData.getVersion(),
					           installedVersion );
					return true;
				}
				break;
			default:
				break;
		}

		return false;
	}

	private AcrossInstallerRepository getInstallerRepository() {
		if ( installerRepository == null ) {
			installerRepository = AcrossContextUtils
					.getBeanRegistry( contextConfig.getContext() )
					.getBeanOfType( AcrossInstallerRepository.class );

			// As soon as we have retrieved the installer registry, and there is a lock manager,
			// make sure we lock for the remainder of the bootstrap
			if ( bootstrapLockManager != null ) {
				bootstrapLockManager.ensureLocked();
			}
		}

		return installerRepository;
	}

	private InstallerAction determineInstallerAction( InstallerMetaData installerMetaData,
	                                                  ModuleBootstrapConfig moduleConfig ) {
		InstallerSettings contextSettings = contextConfig.getInstallerSettings();
		InstallerAction action = contextSettings.shouldRun( moduleConfig.getModuleName(), installerMetaData );

		if ( action != InstallerAction.DISABLED ) {
			InstallerSettings moduleSettings = moduleConfig.getInstallerSettings();

			if ( moduleSettings != null ) {
				action = moduleSettings.shouldRun( moduleConfig.getModuleName(), installerMetaData );
			}
		}

		return action;
	}

	private Class determineInstallerClass( Object installerOrClass ) {
		return installerOrClass instanceof Class ? (Class) installerOrClass : installerOrClass.getClass();
	}

	private Optional<Object> determineInstallerInstance( Object installerOrClass ) {
		return installerOrClass instanceof Class ? Optional.empty() : Optional.of( installerOrClass );
	}

	private AcrossConfigurableApplicationContext getInstallerContext( AcrossModule module ) {
		boolean created = false;

		if ( !installerContexts.containsKey( module ) ) {
			AcrossConfigurableApplicationContext context = applicationContextFactory.createInstallerContext();
			context.setDisplayName( "Installer context: " + module.getName() );
			installerContexts.put( module, context );

			created = true;
		}

		AcrossConfigurableApplicationContext installerContext = installerContexts.get( module );

		AcrossApplicationContextHolder moduleContext
				= AcrossContextUtils.getAcrossApplicationContextHolder( module );

		// Ensure the installer ApplicationContext has the right parent
		if ( moduleContext == null ) {
			installerContext.setParent( AcrossContextUtils.getApplicationContext( contextConfig.getContext() ) );
		}
		else {
			installerContext.setParent( AcrossContextUtils.getApplicationContext( module ) );
		}

		if ( created ) {
			applicationContextFactory.loadApplicationContext(
					installerContext, contextConfig.getModule( module.getName() ).getInstallerContextConfigurers()
			);
		}

		return installerContext;
	}

	/**
	 * Destroy the installer registry, this will destroy all created installer application context instances.
	 */
	public void destroy() {
		installerContexts.values()
		                 .forEach( c -> {
			                 c.stop();
			                 c.close();
		                 } );
		installerContexts.clear();
	}
}
