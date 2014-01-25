package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossCoreModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtil;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRegistry;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.across.core.installers.InstallerPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Takes care of bootstrapping an entire across context.
 */
public class AcrossBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossBootstrapper.class );

	private final AcrossContext context;
	private BootstrapApplicationContextFactory applicationContextFactory;

	public AcrossBootstrapper( AcrossContext context ) {
		this.context = context;

		applicationContextFactory = new AnnotationConfigBootstrapApplicationContextFactory();
	}

	public BootstrapApplicationContextFactory getApplicationContextFactory() {
		return applicationContextFactory;
	}

	public void setApplicationContextFactory( BootstrapApplicationContextFactory applicationContextFactory ) {
		this.applicationContextFactory = applicationContextFactory;
	}

	/**
	 * Bootstraps all modules in the context.
	 */
	public void bootstrap() {
		runModuleBootstrapCustomizations();

		AcrossApplicationContext root = createRootContext();
		AcrossContextUtil.setAcrossApplicationContext( context, root );

		AbstractApplicationContext rootContext = root.getApplicationContext();
		rootContext.refresh();
		rootContext.start();

		AcrossBeanCopyHelper beanHelper = new AcrossBeanCopyHelper();

		prepareBasicContext( context, rootContext, root.getBeanFactory() );

		createInstallerRepository( context, root.getBeanFactory() );

		Collection<AcrossModule> modulesInOrder = createOrderedModulesList( context );
		AcrossInstallerRegistry installerRegistry = new AcrossInstallerRegistry( context, modulesInOrder );

		// Run installers that don't need anything bootstrapped
		installerRegistry.runInstallers( InstallerPhase.BeforeContextBootstrap );

		LOG.debug( "Bootstrapping {} modules - start", modulesInOrder.size() );

		for ( AcrossModule module : context.getModules() ) {
			LOG.debug( "Bootstrapping {} module", module.getName() );

			// Run installers before bootstrapping this particular module
			installerRegistry.runInstallersForModule( module, InstallerPhase.BeforeModuleBootstrap );

			// Create the module context
			AbstractApplicationContext child =
					applicationContextFactory.createApplicationContext( context, module, root );
			addPostProcessors( child, context.getBeanFactoryPostProcessors() );

			AcrossApplicationContext moduleApplicationContext = new AcrossApplicationContext( child, root );
			AcrossContextUtil.setAcrossApplicationContext( module, moduleApplicationContext );

			if ( !( module instanceof AcrossCoreModule ) ) {
				context.publishEvent( new AcrossModuleBeforeBootstrapEvent( context, module ) );
			}

			applicationContextFactory.loadApplicationContext( context, module, moduleApplicationContext );

			// Bootstrap the module
			module.bootstrap();

			// Send event that this module has bootstrapped
			if ( !( module instanceof AcrossCoreModule ) ) {
				context.publishEvent( new AcrossModuleBootstrappedEvent( context, module ) );
			}

			// Run installers after module itself has bootstrapped
			installerRegistry.runInstallersForModule( module, InstallerPhase.AfterModuleBootstrap );

			// Copy the beans to the parent context
			beanHelper.copy( child, rootContext, module.getExposeFilter() );

			AcrossContextUtil.autoRegisterEventHandlers( child, rootContext.getBean( AcrossEventPublisher.class ) );
		}

		// Bootstrapping done, run installers that require context bootstrap finished
		installerRegistry.runInstallers( InstallerPhase.AfterContextBoostrap );

		LOG.debug( "Bootstrapping {} modules - finished", modulesInOrder.size() );

		if ( rootContext.getParent() != null && rootContext.getParent() instanceof ConfigurableApplicationContext ) {
			pushDefinitionsToParent( beanHelper, (ConfigurableApplicationContext) rootContext.getParent() );
		}

		// Refresh beans
		AcrossContextUtil.refreshBeans( context );

		// Bootstrap finished - publish the event
		context.publishEvent( new AcrossContextBootstrappedEvent( context ) );
	}

	private Collection<AcrossModule> createOrderedModulesList( AcrossContext context ) {
		// Extend this method once the adding order of modules is no longer important, but inter-module
		// dependencies and annotations are used
		return context.getModules();
	}

	private void runModuleBootstrapCustomizations() {
		for ( AcrossModule module : context.getModules() ) {
			if ( module instanceof BootstrapAdapter ) {
				( (BootstrapAdapter) module ).customizeBootstrapper( this );
			}
		}
	}

	private AcrossApplicationContext createRootContext() {
		ApplicationContext parent = AcrossContextUtil.getParentApplicationContext( context );

		AbstractApplicationContext rootApplicationContext =
				applicationContextFactory.createApplicationContext( context, parent );

		return new AcrossApplicationContext( rootApplicationContext );
	}

	private void pushDefinitionsToParent( AcrossBeanCopyHelper beanCopyHelper,
	                                      ConfigurableApplicationContext applicationContext ) {
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

	private void prepareBasicContext( AcrossContext acrossContext,
	                                  ConfigurableApplicationContext applicationContext,
	                                  ConfigurableListableBeanFactory beanFactory ) {
		addPostProcessors( applicationContext, acrossContext.getBeanFactoryPostProcessors() );

		beanFactory.registerSingleton( "acrossContext", acrossContext );

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

	private void createInstallerRepository( AcrossContext context, ConfigurableListableBeanFactory beanFactory ) {
		if ( beanFactory.getBeansOfType( AcrossInstallerRepository.class ).isEmpty() ) {
			AcrossInstallerRepository installerRepository = new AcrossInstallerRepository( context.getDataSource() );
			beanFactory.autowireBeanProperties( installerRepository, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
			beanFactory.initializeBean( installerRepository, "" );
			beanFactory.registerSingleton( AcrossInstallerRepository.class.getName(), installerRepository );
		}
	}

	private void runBaseSchemaInstaller( AcrossContext context, ConfigurableListableBeanFactory beanFactory ) {
		AcrossCoreSchemaInstaller installer = new AcrossCoreSchemaInstaller( context );
		beanFactory.autowireBeanProperties( installer, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
		beanFactory.initializeBean( installer, "" );
	}
}
