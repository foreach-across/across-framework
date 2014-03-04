package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.core.installers.AcrossInstallerRegistry;
import com.foreach.across.core.installers.InstallerPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.*;

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
		checkBootstrapIsPossible();

		checkUniqueModuleNames( context.getModules() );

		Collection<AcrossModule> modulesInOrder = createOrderedModulesList( context );

		LOG.debug( "Bootstrapping {} modules in the following order:", modulesInOrder.size() );
		int order = 1;
		for ( AcrossModule module : modulesInOrder ) {
			LOG.debug( "{} - {}: {}", order++, module.getName(), module.getClass() );
		}

		runModuleBootstrapCustomizations( modulesInOrder );

		AcrossApplicationContext root = createRootContext( modulesInOrder );
		AcrossContextUtils.setAcrossApplicationContext( context, root );

		applicationContextFactory.loadApplicationContext( context, root );

		AbstractApplicationContext rootContext = root.getApplicationContext();

		AcrossBeanCopyHelper beanHelper = new AcrossBeanCopyHelper();

		for ( AcrossModule module : new LinkedList<AcrossModule>( modulesInOrder ) ) {
			module.prepareForBootstrap( modulesInOrder );
		}

		AcrossInstallerRegistry installerRegistry = new AcrossInstallerRegistry( context, modulesInOrder );

		// Run installers that don't need anything bootstrapped
		installerRegistry.runInstallers( InstallerPhase.BeforeContextBootstrap );

		for ( AcrossModule module : modulesInOrder ) {
			LOG.debug( "Bootstrapping {} module", module.getName() );

			Map<String, Object> providedSingletons = new HashMap<String, Object>();
			providedSingletons.put( AcrossModule.CURRENT_MODULE, module );

			// Run installers before bootstrapping this particular module
			installerRegistry.runInstallersForModule( module, InstallerPhase.BeforeModuleBootstrap );

			// Create the module context
			AbstractApplicationContext child =
					applicationContextFactory.createApplicationContext( context, module, root, providedSingletons );

			AcrossApplicationContext moduleApplicationContext = new AcrossApplicationContext( child, root );
			AcrossContextUtils.setAcrossApplicationContext( module, moduleApplicationContext );

			context.publishEvent( new AcrossModuleBeforeBootstrapEvent( context, module ) );

			applicationContextFactory.loadApplicationContext( context, module, moduleApplicationContext );

			// Bootstrap the module
			module.bootstrap();

			// Send event that this module has bootstrapped
			context.publishEvent( new AcrossModuleBootstrappedEvent( context, module ) );

			// Run installers after module itself has bootstrapped
			installerRegistry.runInstallersForModule( module, InstallerPhase.AfterModuleBootstrap );

			// Copy the beans to the parent context
			beanHelper.copy( child, rootContext, module.getExposeFilter(), module.getExposeTransformer() );

			AcrossContextUtils.autoRegisterEventHandlers( child, rootContext.getBean( AcrossEventPublisher.class ) );
		}

		// Bootstrapping done, run installers that require context bootstrap finished
		installerRegistry.runInstallers( InstallerPhase.AfterContextBoostrap );

		LOG.debug( "Bootstrapping {} modules - finished", modulesInOrder.size() );

		if ( rootContext.getParent() != null && rootContext.getParent() instanceof ConfigurableApplicationContext ) {
			pushDefinitionsToParent( beanHelper, (ConfigurableApplicationContext) rootContext.getParent() );
		}

		// Refresh beans
		AcrossContextUtils.refreshBeans( context );

		// Bootstrap finished - publish the event
		context.publishEvent( new AcrossContextBootstrappedEvent( context, modulesInOrder ) );
	}

	private void checkBootstrapIsPossible() {
		if ( context.isAllowInstallers() && context.getDataSource() == null ) {
			throw new RuntimeException(
					"A datasource must be configured if installers are allowed when bootstrapping the AcrossContext" );
		}
	}

	private void checkUniqueModuleNames( Collection<AcrossModule> modules ) {
		Set<String> moduleNames = new HashSet<String>();

		for ( AcrossModule module : modules ) {
			if ( moduleNames.contains( module.getName() ) ) {
				throw new RuntimeException(
						"Each module must have a unique name, duplicate found for " + module.getName() );
			}

			moduleNames.add( module.getName() );
		}
	}

	private Collection<AcrossModule> createOrderedModulesList( AcrossContext context ) {
		return BootstrapAcrossModuleOrder.create( context.getModules(), true );
	}

	private void runModuleBootstrapCustomizations( Collection<AcrossModule> modules ) {
		for ( AcrossModule module : modules ) {
			if ( module instanceof BootstrapAdapter ) {
				( (BootstrapAdapter) module ).customizeBootstrapper( this );
			}
		}
	}

	private AcrossApplicationContext createRootContext( Collection<AcrossModule> modules ) {
		Map<String, Object> providedBeans = new HashMap<String, Object>();

		// Put the context as a fixed singleton
		providedBeans.put( AcrossContext.BEAN, context );

		// Put the modules as singletons in the context
		for ( AcrossModule module : modules ) {
			providedBeans.put( module.getName(), module );
		}

		AbstractApplicationContext rootApplicationContext =
				applicationContextFactory.createApplicationContext( context, context.getParentApplicationContext(),
				                                                    providedBeans );

		return new AcrossApplicationContext( rootApplicationContext );
	}

	private void pushDefinitionsToParent( AcrossBeanCopyHelper beanCopyHelper,
	                                      ConfigurableApplicationContext applicationContext ) {
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		BeanDefinitionRegistry registry = null;

		if ( beanFactory instanceof BeanDefinitionRegistry ) {
			registry = (BeanDefinitionRegistry) beanFactory;
		}

		if ( registry != null ) {
			for ( Map.Entry<String, BeanDefinition> beanDef : beanCopyHelper.getDefinitionsCopied().entrySet() ) {
				registry.registerBeanDefinition( beanDef.getKey(), beanDef.getValue() );
			}
		}

		for ( Map.Entry<String, Object> singleton : beanCopyHelper.getSingletonsCopied().entrySet() ) {
			beanFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
		}
	}
}
