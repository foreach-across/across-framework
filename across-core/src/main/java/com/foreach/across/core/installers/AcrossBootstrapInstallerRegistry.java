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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossEntity;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.BootstrapApplicationContextFactory;
import com.foreach.across.core.context.bootstrap.BootstrapLockManager;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
			Assert.notNull( installerOrClass, "Installer instance should never be null." );

			Class<?> installerClass = determineInstallerClass( installerOrClass );
			Optional<Object> installerInstance = determineInstallerInstance( installerOrClass );

			InstallerMetaData metadata = InstallerMetaData.forClass( installerClass );

			if ( metadata.getInstallerPhase() == phase ) {
				// Create installer instance if necessary
				InstallerAction action = determineInstallerAction( metadata, moduleConfig );
				LOG.trace( "Determined action {} for installer {}.", action, metadata.getName() );

				if ( shouldCheckRunCondition( action ) ) {
					if ( conditionalsMet( moduleConfig.getModule(), installerClass, installerInstance )
							&& shouldPerformAction( action, moduleConfig.getModule(), metadata ) ) {
						takeBootstrapLock();
						try {
							performInstallerAction( action, moduleConfig.getModule(), metadata, installerInstance );
						}
						finally {
							releaseBootstrapLock();
						}
					}
					else {
						LOG.debug( "Skipping installer {} because action {} should not be performed due to bean or run conditions not met.",
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

	/**
	 * Registers the installer bean and checks if the bean definition has been created.
	 */
	private boolean conditionalsMet( AcrossModule module, Class<?> installerClass, Optional<Object> installerInstance ) {
		if ( installerInstance.isPresent() ) {
			// if an instance is passed, regular conditionals do not exist
			return true;
		}

		AcrossConfigurableApplicationContext installerContext = getInstallerContext( module );
		installerContext.register( installerClass );

		// if bean definition is present - conditions have been met
		if ( installerContext.getBeanNamesForType( installerClass, true, false ).length > 0 ) {
			return true;
		}

		LOG.trace( "Skipping installer {} as one or more conditionals have not been met", installerClass );
		return false;
	}

	private void performInstallerAction( InstallerAction action,
	                                     AcrossModule module,
	                                     InstallerMetaData installerMetaData,
	                                     Optional<Object> installerInstance ) {
		AcrossInstallerRepository repository = getInstallerRepository();

		boolean installed = false;
		boolean registerOnly = false;

		if ( action != InstallerAction.REGISTER ) {
			Optional<Object> installer = prepareInstaller( module, installerMetaData.getInstallerClass(), installerInstance );

			if ( installer.isPresent() ) {
				Object target = installer.get();

				if ( action == InstallerAction.EXECUTE && target instanceof InstallerActionResolver ) {
					Optional<InstallerAction> newAction = ( (InstallerActionResolver) target )
							.resolve( module.getName(), installerMetaData );

					if ( newAction.isPresent() && InstallerAction.EXECUTE != newAction.get() ) {
						LOG.info( "Resolved bean installer action, change from {} to {}", action, newAction.get() );
						action = newAction.get();
					}
				}

				if ( action == InstallerAction.EXECUTE || action == InstallerAction.FORCE ) {
					LOG.info( "Executing installer {} for module {}", installerMetaData.getName(), module.getName() );

					ConfigurableListableBeanFactory beanFactory = getBeanFactory( module );
					for ( Method method : installerMetaData.getInstallerMethods() ) {
						try {
							Class<?>[] paramTypes = method.getParameterTypes();
							Object[] arguments = new Object[paramTypes.length];
							if ( method.getParameterCount() > 0 && beanFactory != null ) {
								boolean required = method.getDeclaredAnnotation( InstallerMethod.class ).required();
								Set<String> autowiredBeans = new LinkedHashSet<>( paramTypes.length );
								TypeConverter typeConverter = beanFactory.getTypeConverter();

								for ( int i = 0; i < arguments.length; i++ ) {
									MethodParameter methodParam = new MethodParameter( method, i );
									DependencyDescriptor currDesc = new DependencyDescriptor( methodParam, required );
									currDesc.setContainingClass( target.getClass() );
									String beanName = target.getClass().getName();
									try {
										Object arg = beanFactory.resolveDependency( currDesc, beanName, autowiredBeans, typeConverter );
										arguments[i] = arg;
									}
									catch ( BeansException ex ) {
										throw new UnsatisfiedDependencyException( null, beanName, new InjectionPoint( methodParam ), ex );
									}
								}

							}
							ReflectionUtils.makeAccessible( method );
							method.invoke( target, arguments );
							installed = true;
						}
						catch ( InvocationTargetException ite ) {
							throw new AcrossInstallerException( module.getName(), installerMetaData, target, method, ite.getCause() );
						}
						catch ( Exception e ) {
							throw new AcrossInstallerException( module.getName(), installerMetaData, target, method, e );
						}
					}

					if ( !installed ) {
						LOG.warn( "No @InstallerMethod methods were found for {}", installerMetaData.getName() );
					}
				}
				else if ( action == InstallerAction.REGISTER ) {
					registerOnly = true;
				}

				LOG.trace( "Finished execution of installer {} for module {}", installer.getClass(), module.getName() );
			}
			else {
				LOG.debug(
						"Skipping installer {} for modules {} - instance could not be retrieved (dependencies not met)",
						installerMetaData.getName(), module.getName() );
			}
		}
		else {
			registerOnly = true;
		}

		if ( registerOnly ) {
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
			repository.setInstalled( module.getName(), installerMetaData );
		}

	}

	private ConfigurableListableBeanFactory getBeanFactory( AcrossEntity contextOrModule ) {
		AcrossApplicationContextHolder applicationContextHolder = AcrossContextUtils.getAcrossApplicationContextHolder( contextOrModule );
		return applicationContextHolder != null ? applicationContextHolder.getBeanFactory() : null;
	}

	private Optional<Object> prepareInstaller( AcrossModule module,
	                                           Class<?> installerClass,
	                                           Optional<Object> installerInstance ) {
		AcrossConfigurableApplicationContext installerContext = getInstallerContext( module );

		if ( !installerInstance.isPresent() ) {
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
					"Installer {} for module {} was passed as an instance - this functionality will be removed in future releases.",
					installerClass.getName(), module.getName()
			);

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
		if ( action == InstallerAction.FORCE ) {
			return true;
		}

		switch ( installerMetaData.getRunCondition() ) {
			case AlwaysRun:
				LOG.debug( "Performing action {} for installer {} because it is set to always run", action,
				           installerMetaData.getInstallerClass() );
				return true;
			case VersionDifferent: {
				// Get the installer repository because now we need to perform version lookups
				// and possibly register on execution.  This will also install the core schema if necessary.
				AcrossInstallerRepository repository = getInstallerRepository();

				int installedVersion = repository.getInstalledVersion( module.getName(), installerMetaData.getName() );

				if ( installerMetaData.getVersion() > installedVersion ) {
					// Double check - check again after we have acquired bootstrap lock
					takeBootstrapLock();
					installedVersion = repository.getInstalledVersion( module.getName(), installerMetaData.getName() );
					if ( installerMetaData.getVersion() > installedVersion ) {
						LOG.debug(
								"Performing action {} for installer {} because version {} is higher than installed {}",
								action, installerMetaData.getInstallerClass(), installerMetaData.getVersion(),
								installedVersion );
						return true;
					}
					else {
						LOG.trace(
								"Skipping action {} for installers {} because version {} was not higher than {} after acquiring bootstrap lock",
								action, installerMetaData.getInstallerClass(), installerMetaData.getVersion(),
								installedVersion );
						LOG.trace(
								"Performing an early release of the bootstrap lock since no installer needs executing" );
						releaseBootstrapLock();
					}
				}
				break;
			}
			default:
				break;
		}

		return false;
	}

	private void releaseBootstrapLock() {
		if ( bootstrapLockManager != null ) {
			bootstrapLockManager.ensureUnlocked();
		}
	}

	private void takeBootstrapLock() {
		if ( bootstrapLockManager != null ) {
			bootstrapLockManager.ensureLocked();
		}
	}

	private AcrossInstallerRepository getInstallerRepository() {
		if ( installerRepository == null ) {
			installerRepository = AcrossContextUtils
					.getBeanRegistry( contextConfig.getContext() )
					.getBeanOfType( AcrossInstallerRepository.class );
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
			context.setModuleIndex( contextConfig.getModule( module.getName() ).getBootstrapIndex() );
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
					installerContext, contextConfig.getModule( module.getName() ).getInstallerContextConfigurers(), Collections.emptyList()
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
