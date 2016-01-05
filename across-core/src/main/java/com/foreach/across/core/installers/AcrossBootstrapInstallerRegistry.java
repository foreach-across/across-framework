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
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.annotations.conditions.AcrossDependsCondition;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.BootstrapLockManager;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.database.SchemaConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Builds the list of all installers in a configured AcrossContext.
 * Provides easy methods to run installers in different bootstrap phases.
 */
public class AcrossBootstrapInstallerRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossBootstrapInstallerRegistry.class );

	private final BootstrapLockManager bootstrapLockManager;
	private final AcrossBootstrapConfig contextConfig;

	private AcrossInstallerRepository installerRepository;

	public AcrossBootstrapInstallerRegistry( AcrossBootstrapConfig contextConfig,
	                                         BootstrapLockManager bootstrapLockManager ) {
		this.bootstrapLockManager = bootstrapLockManager;
		this.contextConfig = contextConfig;
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

		for ( Object installer : moduleConfig.getInstallers() ) {
			Assert.notNull( installer, "Installer instance should never be null." );

			Class<?> installerClass = determineInstallerClass( installer );
			Installer metadata = installerClass.getAnnotation( Installer.class );

			if ( metadata == null ) {
				throw new AcrossException( "Installer " + installer.getClass() + " should have @Installer annotation" );
			}

			if ( metadata.phase() == phase ) {
				if ( areDependenciesMet( installerClass ) ) {
					LOG.trace( "Dependencies for installer {} are met.", installerClass );

					// Create installer instance if necessary
					Object instance = determineInstallerInstance( installer );
					InstallerAction action = determineInstallerAction( instance, moduleConfig );
					LOG.trace( "Determined action {} for installer {}.", action, installerClass );

					if ( shouldCheckRunCondition( action ) ) {
						if ( shouldPerformAction( action, moduleConfig.getModule(), installerClass, metadata ) ) {
							if ( shouldPerformSchemaConfiguration( instance ) ) {
								LOG.trace( "Performing schema configuration for installer {}.", installerClass );
								performSchemaConfiguration( instance, moduleConfig.getModule() );
							}
							performInstallerAction( action, moduleConfig.getModule(), instance, metadata );
						}
						else {
							LOG.debug( "Skipping installer {} because action {} should not be performed.",
							           action, installerClass );
						}
					}
					else {
						LOG.debug( "Skipping installer {} because action is {}", installerClass, action );
					}
				}
				else {
					LOG.debug( "Skipping installer {} because dependencies are not met.", installerClass );
				}
			}
			else {
				LOG.trace( "Ignoring installer {} because it is defined for phase {}", installerClass,
				           metadata.phase().name() );
			}
		}

		LOG.trace( "Finished {} installers for module {}", phase.name(), moduleConfig.getModuleName() );
	}

	private void performInstallerAction( InstallerAction action,
	                                     AcrossModule module,
	                                     Object installer,
	                                     Installer metadata ) {
		AcrossInstallerRepository repository = getInstallerRepository();

		if ( action != InstallerAction.REGISTER ) {
			LOG.info( "Executing installer {} for module {}", installer.getClass(), module.getName() );

			ConfigurableListableBeanFactory beanFactory = getBeanFactoryForInstallerWiring( module );

			beanFactory.autowireBeanProperties( installer, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
			beanFactory.initializeBean( installer, "" );

			boolean executed = false;

			for ( Method method : ReflectionUtils.getAllDeclaredMethods( installer.getClass() ) ) {
				if ( method.isAnnotationPresent(
						InstallerMethod.class ) && method.getParameterTypes().length == 0 ) {
					try {
						method.setAccessible( true );
						method.invoke( installer );

						executed = true;
					}
					catch ( Exception e ) {
						throw new AcrossException( e );
					}
				}
			}

			if ( !executed ) {
				throw new AcrossException(
						"At least one @InstallerMethod method was expected for " + installer.getClass() );
			}

			LOG.trace( "Finished execution of installer {} for module {}", installer.getClass(), module.getName() );
		}
		else {
			// Register the installer version
			LOG.info(
					"Only performing registration of installer {} - version {} for module {}",
					installer.getClass(),
					metadata.version(),
					module.getName()
			);
		}

		repository.setInstalled( module, metadata, installer.getClass() );
	}

	private boolean shouldCheckRunCondition( InstallerAction action ) {
		return action != InstallerAction.DISABLED && action != InstallerAction.SKIP;
	}

	private boolean shouldPerformAction( InstallerAction action,
	                                     AcrossModule module,
	                                     Class<?> installerClass,
	                                     Installer metadata ) {
		// Get the installer repository because now we need to perform version lookups
		// and possibly register on execution.  This will also install the core schema if necessary.
		AcrossInstallerRepository repository = getInstallerRepository();

		if ( action == InstallerAction.FORCE ) {
			return true;
		}

		switch ( metadata.runCondition() ) {
			case AlwaysRun:
				LOG.debug( "Performing action {} for installer {} because it is set to always run", action,
				           installerClass );
				return true;
			case VersionDifferent:
				int installedVersion = repository.getInstalledVersion( module, installerClass );
				if ( metadata.version() > installedVersion ) {
					LOG.debug( "Performing action {} for installer {} because version {} is higher than installed {}",
					           action, installerClass, metadata.version(), installedVersion );
					return true;
				}
				break;
			default:
				break;
		}

		return false;
	}

	private boolean shouldPerformSchemaConfiguration( Object installer ) {
		return installer instanceof AcrossLiquibaseInstaller;
	}

	private void performSchemaConfiguration( Object installer, AcrossModule module ) {
		SchemaConfiguration schemaConfiguration = determineSchemaConfiguration( module );

		if ( schemaConfiguration != null && installer instanceof AcrossLiquibaseInstaller ) {
			AcrossLiquibaseInstaller liquibaseInstaller = (AcrossLiquibaseInstaller) installer;
			liquibaseInstaller.setSchemaConfiguration( schemaConfiguration );
		}
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

	private InstallerAction determineInstallerAction( Object installer, ModuleBootstrapConfig moduleConfig ) {
		InstallerSettings contextSettings = contextConfig.getInstallerSettings();

		// Search InstallerGroup annotation up the hierarchy, as it can be inherited
		Class<?> installerClass = installer.getClass();
		InstallerGroup groupAnnotation = AnnotationUtils.findAnnotation( installerClass, InstallerGroup.class );

		String group = groupAnnotation != null ? groupAnnotation.value() : null;

		InstallerAction action = contextSettings.shouldRun( group, installer );

		if ( action != InstallerAction.DISABLED ) {
			InstallerSettings moduleSettings = moduleConfig.getInstallerSettings();

			if ( moduleSettings != null ) {
				action = moduleSettings.shouldRun( group, installer );
			}
		}

		return action;
	}

	private Class determineInstallerClass( Object installerOrClass ) {
		return installerOrClass instanceof Class ? (Class) installerOrClass : installerOrClass.getClass();
	}

	private Object determineInstallerInstance( Object installerOrClass ) {
		try {
			if ( installerOrClass instanceof Class ) {
				Class<?> installerClass = (Class<?>) installerOrClass;

				Constructor<?> c = installerClass.getDeclaredConstructor();
				ReflectionUtils.makeAccessible( c );

				return c.newInstance();
			}

			return installerOrClass;
		}
		catch ( InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ie ) {
			throw new AcrossException( "Could not create installer instance: " + installerOrClass, ie );
		}
	}

	private SchemaConfiguration determineSchemaConfiguration( AcrossModule module ) {
		ConfigurableListableBeanFactory beanFactory = AcrossContextUtils.getBeanFactory( contextConfig.getContext() );

		SchemaConfiguration defaultSchemaConfiguration = null;
		SchemaConfiguration moduleSchemaConfiguration = null;

		Map<String, SchemaConfiguration> schemaConfigurations = beanFactory.getBeansOfType( SchemaConfiguration.class );

		for ( Map.Entry<String, SchemaConfiguration> schemaConfigurationEntry : schemaConfigurations.entrySet() ) {
			String schemaConfigModule = getModuleAnnotationValue( schemaConfigurationEntry.getKey(), beanFactory );

			if ( StringUtils.isBlank( schemaConfigModule ) && defaultSchemaConfiguration == null ) {
				defaultSchemaConfiguration = schemaConfigurationEntry.getValue();
			}
			else if ( StringUtils.equals( schemaConfigModule,
			                              module.getName() ) && moduleSchemaConfiguration == null ) {
				moduleSchemaConfiguration = schemaConfigurationEntry.getValue();
			}
		}

		return moduleSchemaConfiguration != null ? moduleSchemaConfiguration : defaultSchemaConfiguration;
	}

	private String getModuleAnnotationValue( String beanName, ConfigurableListableBeanFactory beanFactory ) {
		String moduleAnnotationValue = null;

		// First look for the module annotation on the factory method
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition( beanName );
		if ( beanDefinition instanceof AnnotatedBeanDefinition ) {
			AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
			Map<String, Object> moduleAnnotationAttributes =
					annotatedBeanDefinition.getFactoryMethodMetadata().getAnnotationAttributes(
							Module.class.getName() );
			for ( Map.Entry<String, Object> attributeEntry : moduleAnnotationAttributes.entrySet() ) {
				if ( StringUtils.equals( attributeEntry.getKey(), "value" ) ) {
					String moduleName = attributeEntry.getValue().toString();
					if ( StringUtils.isNotBlank( moduleName ) ) {
						moduleAnnotationValue = moduleName;
						break;
					}
				}
			}
		}

		// If nothing is found then look for the module annotation on the bean class
		Module moduleConfig = beanFactory.findAnnotationOnBean( beanName, Module.class );
		if ( moduleConfig != null && StringUtils.isBlank( moduleAnnotationValue ) ) {
			moduleAnnotationValue = moduleConfig.value();
		}

		return moduleAnnotationValue;
	}

	private boolean areDependenciesMet( Class<?> installerClass ) {
		return AcrossDependsCondition.applies( contextConfig, installerClass );
	}

	private ConfigurableListableBeanFactory getBeanFactoryForInstallerWiring( AcrossModule module ) {
		AcrossApplicationContextHolder moduleContext =
				AcrossContextUtils.getAcrossApplicationContextHolder( module );

		if ( moduleContext == null ) {
			// If module context not yet available, use the root context
			return AcrossContextUtils.getBeanFactory( contextConfig.getContext() );
		}
		else {
			return moduleContext.getBeanFactory();
		}
	}
}
