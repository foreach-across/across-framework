package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossException;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Builds the list of all installers in a configured AcrossContext.
 * Provides easy methods to run installers in different bootstrap phases.
 */
public class AcrossInstallerRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossInstallerRegistry.class );

	private final AcrossBootstrapConfig contextConfig;

	private AcrossInstallerRepository installerRepository;

	public AcrossInstallerRegistry( AcrossBootstrapConfig contextConfig ) {
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
		AcrossInstallerRepository installerRepository = getInstallerRepository();

		if ( action != InstallerAction.REGISTER ) {
			LOG.debug( "Executing installer {} for module {}", installer.getClass(), module.getName() );

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
			LOG.trace(
					"Only performing registration of installer {} - version {} for module {}",
					installer.getClass(),
					metadata.version(),
					module.getName()
			);
		}

		installerRepository.setInstalled( module, metadata, installer.getClass() );
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
		AcrossInstallerRepository installerRepository = getInstallerRepository();

		if ( action == InstallerAction.FORCE ) {
			return true;
		}

		switch ( metadata.runCondition() ) {
			case AlwaysRun:
				LOG.debug( "Performing action {} for installer {} because it is set to always run", action,
				           installerClass );
				return true;
			case VersionDifferent:
				int installedVersion = installerRepository.getInstalledVersion( module, installerClass );
				if ( metadata.version() > installedVersion ) {
					LOG.debug( "Performing action {} for installer {} because version {} is higher than installed {}",
					           action, installerClass, metadata.version(), installedVersion );
					return true;
				}
				break;
		}

		return false;
	}

	private AcrossInstallerRepository getInstallerRepository() {
		if ( installerRepository == null ) {
			installerRepository = AcrossContextUtils.getBeanOfType( contextConfig.getContext(),
			                                                        AcrossInstallerRepository.class );
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
			return installerOrClass instanceof Class ? ( (Class) installerOrClass ).newInstance() : installerOrClass;
		}
		catch ( InstantiationException | IllegalAccessException ie ) {
			throw new AcrossException( "Could not create installer instance: " + installerOrClass, ie );
		}
	}

	private boolean areDependenciesMet( Class<?> installerClass ) {
		return true;
	}

	private ConfigurableListableBeanFactory getBeanFactoryForInstallerWiring( AcrossModule module ) {
		AcrossApplicationContext moduleContext =
				AcrossContextUtils.getAcrossApplicationContext( module );

		if ( moduleContext == null ) {
			// If module context not yet available, use the root context
			return AcrossContextUtils.getBeanFactory( contextConfig.getContext() );
		}
		else {
			return moduleContext.getBeanFactory();
		}
	}
}
