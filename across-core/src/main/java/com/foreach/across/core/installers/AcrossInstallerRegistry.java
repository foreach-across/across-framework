package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Builds the list of all installers in a configured AcrossContext.
 * Provides easy methods to run installers in different bootstrap phases.
 */
public class AcrossInstallerRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossInstallerRegistry.class );

	private final AcrossInstallerRepository installerRepository;
	private final AcrossContext context;

	private final Collection<ModuleInstallationConfig> installationConfigs;

	private final boolean enabled;

	public AcrossInstallerRegistry( AcrossContext context, Collection<ModuleBootstrapConfig> modulesInOrder ) {
		this.context = context;

		if ( context.isAllowInstallers() ) {
			installerRepository = AcrossContextUtils.getBeanOfType( context, AcrossInstallerRepository.class );
			installationConfigs = buildInstallationConfigs( modulesInOrder );
		}
		else {
			installerRepository = null;
			installationConfigs = Collections.emptyList();
		}

		enabled = installerRepository != null;
	}

	private Collection<ModuleInstallationConfig> buildInstallationConfigs( Collection<ModuleBootstrapConfig> modules ) {
		List<ModuleInstallationConfig> list = new ArrayList<ModuleInstallationConfig>( modules.size() );

		for ( ModuleBootstrapConfig bootstrapConfig : modules ) {
			ModuleInstallationConfig config = new ModuleInstallationConfig( bootstrapConfig );
			config.setName( bootstrapConfig.getModuleName() );

			if ( context.isAllowInstallers() ) {
				for ( Object installer : bootstrapConfig.getInstallers() ) {
					Installer doc = installer.getClass().getAnnotation( Installer.class );

					if ( doc == null ) {
						throw new RuntimeException(
								"Installer " + installer.getClass() + " should have @Installer annotation" );
					}

					boolean methodFound = false;
					for ( Method method : ReflectionUtils.getAllDeclaredMethods( installer.getClass() ) ) {
						if ( method.isAnnotationPresent(
								InstallerMethod.class ) && method.getParameterTypes().length == 0 ) {
							methodFound = true;
						}
					}

					if ( !methodFound ) {
						throw new RuntimeException(
								"Installer " + installer.getClass() + " should have at least one method without parameters annotated with @InstallerMethod" );
					}

					InstallerGroup group = installer.getClass().getAnnotation( InstallerGroup.class );

					if ( group == null || !Arrays.asList( context.getSkipInstallerGroups() ).contains(
							group.value() ) ) {
						if ( doc.runCondition() == InstallerRunCondition.AlwaysRun ) {
							LOG.debug( "Installing {} because it is set to always run", installer );
							config.addInstaller( installer );
						}
						else if ( doc.runCondition() == InstallerRunCondition.VersionDifferent ) {
							int installedVersion =
									installerRepository.getInstalledVersion( config.getBootstrapConfig().getModule(),
									                                         installer );
							int currentVersion = doc.version();

							if ( currentVersion > installedVersion ) {
								LOG.debug( "Installing {} because version {} is higher than currently installed {}",
								           installer, currentVersion, installedVersion );
								config.addInstaller( installer );
							}
						}
					}
					else {
						LOG.debug( "Skipping installer {} because it is in group {}", installer, group.value() );
					}
				}
			}
			else {
				LOG.info( "Skipping all installers because setting on ApplicationContext" );
			}

			list.add( config );
		}

		return list;
	}

	/**
	 * Runs all installers for all modules in the context.
	 *
	 * @param phase Bootstrap phase for installers.
	 */
	public void runInstallers( InstallerPhase phase ) {
		if ( enabled ) {
			for ( ModuleInstallationConfig config : installationConfigs ) {
				runInstallers( config, phase );
			}
		}
	}

	/**
	 * Runs all installers for the given module and bootstrap phase.
	 *
	 * @param module ModuleBootstrapConfig instance.
	 * @param phase  Bootstrap phase for installers.
	 */
	public void runInstallersForModule( ModuleBootstrapConfig module, InstallerPhase phase ) {
		if ( enabled ) {
			runInstallers( findConfigForModule( module ), phase );
		}
	}

	private void runInstallers( ModuleInstallationConfig config, InstallerPhase phase ) {
		ConfigurableListableBeanFactory beanFactory = getBeanFactoryForInstallerWiring( config );

		for ( Object installer : config.getInstallers() ) {
			Installer doc = installer.getClass().getAnnotation( Installer.class );

			if ( doc.phase() == phase ) {

				if ( !context.isOnlyRegisterInstallers() ) {
					LOG.debug( "Running installer {}", installer );

					beanFactory.autowireBeanProperties( installer, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
					beanFactory.initializeBean( installer, "" );

					for ( Method method : ReflectionUtils.getAllDeclaredMethods( installer.getClass() ) ) {
						if ( method.isAnnotationPresent(
								InstallerMethod.class ) && method.getParameterTypes().length == 0 ) {
							try {
								method.setAccessible( true );
								method.invoke( installer );
							}
							catch ( Exception e ) {
								throw new RuntimeException( e );
							}
						}
					}
				}
				else {
					LOG.info( "Registering installer {} instead of actually running it", installer );
				}

				installerRepository.setInstalled( config.getBootstrapConfig().getModule(), doc, installer );
			}
		}
	}

	private ModuleInstallationConfig findConfigForModule( ModuleBootstrapConfig module ) {
		for ( ModuleInstallationConfig config : installationConfigs ) {
			if ( config.getBootstrapConfig() == module ) {
				return config;
			}
		}

		throw new RuntimeException( "Module not registered in the AcrossContext: " + module );
	}

	private ConfigurableListableBeanFactory getBeanFactoryForInstallerWiring( ModuleInstallationConfig config ) {
		AcrossApplicationContext moduleContext =
				AcrossContextUtils.getAcrossApplicationContext( config.getBootstrapConfig().getModule() );

		if ( moduleContext == null ) {
			// If module context not yet available, use the root context
			return AcrossContextUtils.getBeanFactory( context );
		}
		else {
			return moduleContext.getBeanFactory();
		}
	}

	static class ModuleInstallationConfig
	{
		private String name;
		private final ModuleBootstrapConfig bootstrapConfig;
		private final Collection<Object> installers = new LinkedList<Object>();

		ModuleInstallationConfig( ModuleBootstrapConfig bootstrapConfig ) {
			this.bootstrapConfig = bootstrapConfig;
		}

		public Collection<Object> getInstallers() {
			return installers;
		}

		public void addInstaller( Object installer ) {
			installers.add( installer );
		}

		public ModuleBootstrapConfig getBootstrapConfig() {
			return bootstrapConfig;
		}

		public String getName() {
			return name;
		}

		public void setName( String name ) {
			this.name = name;
		}
	}
}
