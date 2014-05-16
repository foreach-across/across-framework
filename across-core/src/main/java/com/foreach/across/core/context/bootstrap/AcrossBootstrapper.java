package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.beans.PrimarySingletonBean;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.ProvidedBeansConfigurer;
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
		AbstractApplicationContext rootContext = root.getApplicationContext();

		Map<AcrossModule, ModuleBootstrapConfig> moduleBootstrapConfigs = createModuleBootstrapConfig( modulesInOrder );
		prepareForBootstrap( moduleBootstrapConfigs );

		AcrossInstallerRegistry installerRegistry =
				new AcrossInstallerRegistry( context, moduleBootstrapConfigs.values() );

		// Run installers that don't need anything bootstrapped
		installerRegistry.runInstallers( InstallerPhase.BeforeContextBootstrap );

		AcrossBeanCopyHelper beanHelper = new AcrossBeanCopyHelper();

		for ( ModuleBootstrapConfig config : moduleBootstrapConfigs.values() ) {
			LOG.debug( "Bootstrapping {} module", config.getModuleName() );

			context.publishEvent( new AcrossModuleBeforeBootstrapEvent( context, config ) );

			// Run installers before bootstrapping this particular module
			installerRegistry.runInstallersForModule( config, InstallerPhase.BeforeModuleBootstrap );

			// Create the module context
			AbstractApplicationContext child =
					applicationContextFactory.createApplicationContext( context, config, root );

			AcrossApplicationContext moduleApplicationContext = new AcrossApplicationContext( child, root );
			AcrossContextUtils.setAcrossApplicationContext( config.getModule(), moduleApplicationContext );

			applicationContextFactory.loadApplicationContext( context, config, moduleApplicationContext );

			// Bootstrap the module
			config.getModule().bootstrap();

			// Send event that this module has bootstrapped
			context.publishEvent( new AcrossModuleBootstrappedEvent( context, config.getModule() ) );

			// Run installers after module itself has bootstrapped
			installerRegistry.runInstallersForModule( config, InstallerPhase.AfterModuleBootstrap );

			// Copy the beans to the parent context
			beanHelper.copy( child, rootContext, config.getExposeFilter(), config.getExposeTransformer() );

			AcrossContextUtils.autoRegisterEventHandlers( child, rootContext.getBean( AcrossEventPublisher.class ) );
		}

		LOG.debug( "Bootstrapping {} modules - finished", modulesInOrder.size() );

		if ( rootContext.getParent() != null && rootContext.getParent() instanceof ConfigurableApplicationContext ) {
			pushDefinitionsToParent( beanHelper, (ConfigurableApplicationContext) rootContext.getParent() );
		}

		// Refresh beans
		AcrossContextUtils.refreshBeans( context );

		// Bootstrapping done, run installers that require context bootstrap finished
		installerRegistry.runInstallers( InstallerPhase.AfterContextBootstrap );

		// Bootstrap finished - publish the event
		context.publishEvent( new AcrossContextBootstrappedEvent( context, modulesInOrder ) );
	}

	private void prepareForBootstrap( Map<AcrossModule, ModuleBootstrapConfig> configs ) {
		for ( AcrossModule module : new LinkedList<AcrossModule>( configs.keySet() ) ) {
			module.prepareForBootstrap( configs.get( module ), configs );
		}
	}

	/**
	 * Create map of all modules and their corresponding config in order.
	 */
	private Map<AcrossModule, ModuleBootstrapConfig> createModuleBootstrapConfig( Collection<AcrossModule> modulesInOrder ) {
		Map<AcrossModule, ModuleBootstrapConfig> configs = new LinkedHashMap<AcrossModule, ModuleBootstrapConfig>();

		for ( AcrossModule module : modulesInOrder ) {
			ModuleBootstrapConfig config = new ModuleBootstrapConfig( module );
			config.setExposeFilter( module.getExposeFilter() );
			config.setExposeTransformer( module.getExposeTransformer() );
			config.getInstallers().addAll( Arrays.asList( module.getInstallers() ) );

			// Provide the current module bean
			Map<String, Object> providedSingletons = new HashMap<String, Object>();
			providedSingletons.put( AcrossModule.CURRENT_MODULE, new PrimarySingletonBean( module ) );

			config.addApplicationContextConfigurer( new ProvidedBeansConfigurer( providedSingletons ) );
			config.addApplicationContextConfigurers( AcrossContextUtils.getConfigurersToApply( context, module ) );

			configs.put( module, config );
		}

		return configs;
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
		AbstractApplicationContext rootApplicationContext =
				applicationContextFactory.createApplicationContext( context, context.getParentApplicationContext() );

		ProvidedBeansMap providedBeans = new ProvidedBeansMap();

		// Put the context as a fixed singleton
		providedBeans.put( AcrossContext.BEAN, new PrimarySingletonBean( context ) );

		// Put the modules as singletons in the context
		for ( AcrossModule module : modules ) {
			providedBeans.put( module.getName(), new PrimarySingletonBean( module ) );
		}

		context.addApplicationContextConfigurer( new ProvidedBeansConfigurer( providedBeans ),
		                                         ConfigurerScope.CONTEXT_ONLY );

		AcrossApplicationContext root = new AcrossApplicationContext( rootApplicationContext );
		AcrossContextUtils.setAcrossApplicationContext( context, root );

		applicationContextFactory.loadApplicationContext( context, root );

		return root;
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
