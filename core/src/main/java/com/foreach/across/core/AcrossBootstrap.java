package com.foreach.across.core;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.core.util.AcrossContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Takes care of bootstrapping an entire across context or a specific module added
 * to an already bootstrapped context.
 */
class AcrossBootstrap
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossBootstrap.class );

	static class ModuleInstallationConfig
	{
		private String name;
		private final AcrossModule module;
		private final Collection<Object> installers = new LinkedList<Object>();

		ModuleInstallationConfig( AcrossModule module ) {
			this.module = module;
		}

		public Collection<Object> getInstallers() {
			return installers;
		}

		public void addInstaller( Object installer ) {
			installers.add( installer );
		}

		public AcrossModule getModule() {
			return module;
		}

		public String getName() {
			return name;
		}

		public void setName( String name ) {
			this.name = name;
		}
	}

	private final AcrossBootstrapApplicationContextHandler handler;

	private ConfigurableApplicationContext applicationContext;

	AcrossBootstrap( AcrossBootstrapApplicationContextHandler handler ) {
		this.handler = handler;
	}

	/**
	 * Bootstraps all modules in the context.
	 */
	public void bootstrap( AcrossContext context ) {
		applicationContext = (ConfigurableApplicationContext) context.getApplicationContext();

		applicationContext.refresh();
		applicationContext.start();

		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

		ConfigurableApplicationContext parentContext = (ConfigurableApplicationContext) applicationContext.getParent();

		AcrossBeanCopyHelper beanHelper = new AcrossBeanCopyHelper();

		prepareBasicContext( context, applicationContext, beanFactory );

		AcrossInstallerRepository installerRepository = fetchInstallerRepository( context, beanFactory );
		Collection<ModuleInstallationConfig> installationConfigs =
				buildModuleConfigs( context, installerRepository, context.getModules() );

		// Run installers that don't need anything bootstrapped
		runInstallers( context, beanFactory, installerRepository, installationConfigs,
		               InstallerPhase.BeforeContextBootstrap );

		LOG.debug( "Bootstrapping {} modules - start", installationConfigs.size() );

		for ( ModuleInstallationConfig config : installationConfigs ) {
			AcrossModule module = config.getModule();

			LOG.debug( "Bootstrapping {} module", config.getName() );

			// Run installers before bootstrapping this particular module
			runInstallers( context, beanFactory, installerRepository, config, InstallerPhase.BeforeModuleBootstrap );

			// Create the module context
			ConfigurableApplicationContext child = handler.createModuleApplicationContext( applicationContext, module );
			addPostProcessors( child, context.getBeanFactoryPostProcessors() );

			child.refresh();
			child.start();

			module.setContext( context );
			module.setApplicationContext( child );

			// Bootstrap the module
			module.bootstrap();

			// Send event that this module has bootstrapped
			if ( !( module instanceof AcrossCoreModule ) ) {
				context.publishEvent( new AcrossModuleBootstrappedEvent( context, module ) );
			}

			// Run installers after module itself has bootstrapped
			runInstallers( context, child.getAutowireCapableBeanFactory(), installerRepository, config,
			               InstallerPhase.AfterModuleBootstrap );

			// Copy the beans to the parent context
			beanHelper.copy( child, applicationContext, module.getExposeFilter() );

			AcrossContextUtil.autoRegisterEventHandlers( child,
			                                             applicationContext.getBean( AcrossEventPublisher.class ) );
		}

		// Bootstrapping done, run installers that require context bootstrap finished
		runInstallers( context, beanFactory, installerRepository, installationConfigs,
		               InstallerPhase.AfterContextBoostrap );

		LOG.debug( "Bootstrapping {} modules - finished", installationConfigs.size() );

		if ( parentContext != null ) {
			pushDefinitionsToParent( beanHelper, parentContext );
		}

		// Refresh beans
		AcrossContextUtil.refreshBeans( context );

		// Bootstrap finished - publish the event
		context.publishEvent( new AcrossContextBootstrappedEvent( context ) );
	}

	private void pushDefinitionsToParent( AcrossBeanCopyHelper beanCopyHelper,
	                                      ConfigurableApplicationContext applicationContext ) {
/*
		for ( ApplicationListener listener : beanCopyHelper.getApplicationListeners() ) {
			applicationContext.addApplicationListener( listener );
		}
*/
		for ( Map.Entry<String, Object> singleton : beanCopyHelper.getSingletonsCopied().entrySet() ) {
			applicationContext.getBeanFactory().registerSingleton( singleton.getKey(), singleton.getValue() );
		}

		if ( applicationContext instanceof GenericApplicationContext ) {
			for ( Map.Entry<String, BeanDefinition> beanDef : beanCopyHelper.getDefinitionsCopied().entrySet() ) {
				( (GenericApplicationContext) applicationContext ).registerBeanDefinition( beanDef.getKey(),
				                                                                           beanDef.getValue() );
			}
		}
	}

	private void runInstallers( AcrossContext context,
	                            AutowireCapableBeanFactory beanFactory,
	                            AcrossInstallerRepository repository,
	                            Collection<ModuleInstallationConfig> installationConfigs,
	                            InstallerPhase phase ) {
		for ( ModuleInstallationConfig config : installationConfigs ) {
			runInstallers( context, beanFactory, repository, config, phase );
		}
	}

	private void runInstallers( AcrossContext context,
	                            AutowireCapableBeanFactory beanFactory,
	                            AcrossInstallerRepository repository,
	                            ModuleInstallationConfig config,
	                            InstallerPhase phase ) {
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

				repository.setInstalled( config.getModule(), doc, installer );

			}
		}
	}

	private Collection<ModuleInstallationConfig> buildModuleConfigs( AcrossContext context,
	                                                                 AcrossInstallerRepository repository,
	                                                                 Collection<AcrossModule> modules ) {
		List<ModuleInstallationConfig> list = new ArrayList<ModuleInstallationConfig>( modules.size() );

		for ( AcrossModule module : modules ) {
			ModuleInstallationConfig config = new ModuleInstallationConfig( module );
			config.setName( module.getName() );

			if ( context.isAllowInstallers() ) {
				Object[] installers = module.getInstallers();

				for ( Object installer : installers ) {
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
							int installedVersion = repository.getInstalledVersion( config.getModule(), installer );
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

	private void prepareBasicContext( AcrossContext acrossContext,
	                                  ConfigurableApplicationContext applicationContext,
	                                  ConfigurableListableBeanFactory beanFactory ) {
		addPostProcessors( applicationContext, acrossContext.getBeanFactoryPostProcessors() );

		beanFactory.registerSingleton( "acrossContext", acrossContext );

		//applicationContext.refresh();
		//applicationContext.start();

		registerDataSource( acrossContext, beanFactory );

		runBaseSchemaInstaller( acrossContext, beanFactory );

		registerCoreModule( acrossContext, beanFactory );
	}

	private void addPostProcessors( ConfigurableApplicationContext applicationContext,
	                                BeanFactoryPostProcessor[] postProcessors ) {
		for ( BeanFactoryPostProcessor postProcessor : postProcessors ) {
			applicationContext.addBeanFactoryPostProcessor( postProcessor );
		}
	}

	private void registerDataSource( AcrossContext context, ConfigurableListableBeanFactory beanFactory ) {
		if ( !Arrays.asList( beanFactory.getSingletonNames() ).contains( AcrossContext.DATASOURCE ) ) {
			beanFactory.registerSingleton( AcrossContext.DATASOURCE, context.getDataSource() );
		}
	}

	private void registerCoreModule( AcrossContext context, ConfigurableListableBeanFactory beanFactory ) {
		AcrossModule coreModule = context.getModules().iterator().next();

		if ( coreModule == null || !( coreModule instanceof AcrossCoreModule ) ) {
			throw new RuntimeException(
					"Unable to bootstrap Across without the first module being the AcrossCoreModule.  Has it been explicitly removed?" );
		}

		if ( beanFactory.getBeansOfType( AcrossCoreModule.class ).isEmpty() ) {
			beanFactory.autowireBeanProperties( coreModule, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
			beanFactory.initializeBean( coreModule, "acrossCoreModule" );
			beanFactory.registerSingleton( "acrossCoreModule", coreModule );
		}
	}

	private AcrossInstallerRepository fetchInstallerRepository( AcrossContext context,
	                                                            ConfigurableListableBeanFactory beanFactory ) {
		if ( beanFactory.getBeansOfType( AcrossInstallerRepository.class ).isEmpty() ) {
			AcrossInstallerRepository installerRepository = new AcrossInstallerRepository( context.getDataSource() );
			beanFactory.autowireBeanProperties( installerRepository, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
			beanFactory.initializeBean( installerRepository, "" );
			beanFactory.registerSingleton( AcrossInstallerRepository.class.getName(), installerRepository );
		}

		return beanFactory.getBean( AcrossInstallerRepository.class );
	}

	private void runBaseSchemaInstaller( AcrossContext context, ConfigurableListableBeanFactory beanFactory ) {
		AcrossCoreSchemaInstaller installer = new AcrossCoreSchemaInstaller( context );
		beanFactory.autowireBeanProperties( installer, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( installer, "" );
	}
}
