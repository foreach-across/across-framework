package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.beans.PrimarySingletonBean;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.ProvidedBeansConfigurer;
import com.foreach.across.core.context.info.*;
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
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
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

		ConfigurableAcrossContextInfo contextInfo = buildContextAndModuleInfo();
		Collection<AcrossModuleInfo> modulesInOrder = contextInfo.getModules();

		LOG.debug( "Bootstrapping {} modules in the following order:", modulesInOrder.size() );
		for ( AcrossModuleInfo moduleInfo : modulesInOrder ) {
			LOG.debug( "{} - {}: {}", moduleInfo.getIndex(), moduleInfo.getName(), moduleInfo.getModule().getClass() );
		}

		runModuleBootstrapperCustomizations( modulesInOrder );

		AcrossApplicationContext root = createRootContext( contextInfo );
		AbstractApplicationContext rootContext = root.getApplicationContext();

		createBootstrapConfiguration( contextInfo );
		prepareForBootstrap( contextInfo );

		AcrossInstallerRegistry installerRegistry =
				new AcrossInstallerRegistry( contextInfo.getBootstrapConfiguration() );

		// Run installers that don't need anything bootstrapped
		installerRegistry.runInstallers( InstallerPhase.BeforeContextBootstrap );

		AcrossBeanCopyHelper beanHelper = new AcrossBeanCopyHelper();
		beanHelper.addSingleton( "acrossContextInfo", contextInfo );

		for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
			ConfigurableAcrossModuleInfo configurableAcrossModuleInfo = (ConfigurableAcrossModuleInfo) moduleInfo;
			ModuleBootstrapConfig config = moduleInfo.getBootstrapConfiguration();
			LOG.debug( "Bootstrapping {} module", config.getModuleName() );

			configurableAcrossModuleInfo.setBootstrapStatus( ModuleBootstrapStatus.BootstrapBusy );

			context.publishEvent( new AcrossModuleBeforeBootstrapEvent( contextInfo, moduleInfo ) );

			// Run installers before bootstrapping this particular module
			installerRegistry.runInstallersForModule( moduleInfo.getName(), InstallerPhase.BeforeModuleBootstrap );

			// Create the module context
			AbstractApplicationContext child =
					applicationContextFactory.createApplicationContext( context, config, root );

			AcrossApplicationContext moduleApplicationContext = new AcrossApplicationContext( child, root );
			AcrossContextUtils.setAcrossApplicationContext( config.getModule(), moduleApplicationContext );

			applicationContextFactory.loadApplicationContext( context, config, moduleApplicationContext );

			// Bootstrap the module
			config.getModule().bootstrap();

			configurableAcrossModuleInfo.setBootstrapStatus( ModuleBootstrapStatus.Bootstrapped );

			// Send event that this module has bootstrapped
			context.publishEvent( new AcrossModuleBootstrappedEvent( moduleInfo ) );

			// Run installers after module itself has bootstrapped
			installerRegistry.runInstallersForModule( moduleInfo.getName(), InstallerPhase.AfterModuleBootstrap );

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
		context.publishEvent( new AcrossContextBootstrappedEvent( contextInfo ) );
	}

	private ConfigurableAcrossContextInfo buildContextAndModuleInfo() {
		ConfigurableAcrossContextInfo contextInfo = new ConfigurableAcrossContextInfo( context );
		ModuleBootstrapOrderBuilder moduleBootstrapOrderBuilder =
				new ModuleBootstrapOrderBuilder( context.getModules() );

		Collection<AcrossModuleInfo> configured = new LinkedList<>();

		int row = 1;
		for ( AcrossModule module : moduleBootstrapOrderBuilder.getOrderedModules() ) {
			configured.add( new ConfigurableAcrossModuleInfo( contextInfo, module, row++ ) );
		}

		contextInfo.setConfiguredModules( configured );

		for ( AcrossModule module : moduleBootstrapOrderBuilder.getOrderedModules() ) {
			ConfigurableAcrossModuleInfo moduleInfo = contextInfo.getConfigurableModuleInfo( module.getName() );

			moduleInfo.setRequiredDependencies(
					convertToModuleInfo( moduleBootstrapOrderBuilder.getConfiguredRequiredDependencies( module ),
					                     contextInfo ) );
			moduleInfo.setOptionalDependencies(
					convertToModuleInfo( moduleBootstrapOrderBuilder.getConfiguredOptionalDependencies( module ),
					                     contextInfo ) );
			moduleInfo.setModuleRole( moduleBootstrapOrderBuilder.getModuleRole( module ) );
		}

		return contextInfo;
	}

	private Collection<AcrossModuleInfo> convertToModuleInfo( Collection<AcrossModule> list,
	                                                          ConfigurableAcrossContextInfo contextInfo ) {
		Collection<AcrossModuleInfo> infoList = new ArrayList<>( list.size() );

		for ( AcrossModule module : list ) {
			infoList.add( contextInfo.getModuleInfo( module.getName() ) );
		}

		return infoList;
	}

	private void prepareForBootstrap( AcrossContextInfo contextInfo ) {
		for ( ModuleBootstrapConfig moduleConfig : contextInfo.getBootstrapConfiguration().getModules() ) {
			moduleConfig.getModule().prepareForBootstrap( moduleConfig, contextInfo.getBootstrapConfiguration() );
		}
	}

	/**
	 * Builds the bootstrap configuration entities.
	 */
	private void createBootstrapConfiguration( ConfigurableAcrossContextInfo contextInfo ) {
		List<ModuleBootstrapConfig> configs = new LinkedList<>();

		for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
			AcrossModule module = moduleInfo.getModule();
			ModuleBootstrapConfig config = new ModuleBootstrapConfig( module );
			config.setExposeFilter( module.getExposeFilter() );
			config.setExposeTransformer( module.getExposeTransformer() );
			config.setInstallerSettings( module.getInstallerSettings() );
			config.getInstallers().addAll( Arrays.asList( module.getInstallers() ) );

			// Provide the current module bean
			Map<String, Object> providedSingletons = new HashMap<>();
			providedSingletons.put( AcrossModule.CURRENT_MODULE,
			                        new PrimarySingletonBean(
					                        moduleInfo,
					                        new AutowireCandidateQualifier( Module.class.getName(),
					                                                        AcrossModule.CURRENT_MODULE )
			                        )
			);
			providedSingletons.put( AcrossModule.CURRENT_MODULE,
			                        new PrimarySingletonBean(
					                        module,
					                        new AutowireCandidateQualifier( Module.class.getName(),
					                                                        AcrossModule.CURRENT_MODULE )
			                        )
			);

			config.addApplicationContextConfigurer( new ProvidedBeansConfigurer( providedSingletons ) );
			config.addApplicationContextConfigurers( AcrossContextUtils.getConfigurersToApply( context, module ) );

			configs.add( config );

			( (ConfigurableAcrossModuleInfo) moduleInfo ).setBootstrapConfiguration( config );
		}

		AcrossBootstrapConfig contextConfig = new AcrossBootstrapConfig( contextInfo.getContext(), configs );
		contextInfo.setBootstrapConfiguration( contextConfig );
	}

	private void checkBootstrapIsPossible() {
		checkUniqueModuleNames( context.getModules() );
	}

	private void checkUniqueModuleNames( Collection<AcrossModule> modules ) {
		Set<String> moduleNames = new HashSet<>();

		for ( AcrossModule module : modules ) {
			if ( moduleNames.contains( module.getName() ) ) {
				throw new AcrossException(
						"Each module must have a unique name, duplicate found for " + module.getName() );
			}

			moduleNames.add( module.getName() );
		}
	}

	private void runModuleBootstrapperCustomizations( Collection<AcrossModuleInfo> modules ) {
		for ( AcrossModuleInfo moduleInfo : modules ) {
			if ( moduleInfo.getModule() instanceof BootstrapAdapter ) {
				( (BootstrapAdapter) moduleInfo.getModule() ).customizeBootstrapper( this );
			}
		}
	}

	private AcrossApplicationContext createRootContext( AcrossContextInfo contextInfo ) {
		AbstractApplicationContext rootApplicationContext =
				applicationContextFactory.createApplicationContext( context, context.getParentApplicationContext() );

		ProvidedBeansMap providedBeans = new ProvidedBeansMap();

		// Put the context and its info as fixed singletons
		providedBeans.put( AcrossContext.BEAN, new PrimarySingletonBean( context ) );
		providedBeans.put( AcrossContextInfo.BEAN, new PrimarySingletonBean( contextInfo ) );

		// Put the module info as singletons in the context
		for ( AcrossModuleInfo moduleInfo : contextInfo.getConfiguredModules() ) {
			providedBeans.put( "across.module." + moduleInfo.getName(),
			                   new PrimarySingletonBean(
					                   moduleInfo.getModule(),
					                   new AutowireCandidateQualifier( Module.class.getName(), moduleInfo.getName() )
			                   )
			);
			providedBeans.put( moduleInfo.getName(),
			                   new PrimarySingletonBean(
					                   moduleInfo,
					                   new AutowireCandidateQualifier( Module.class.getName(), moduleInfo.getName() )
			                   )
			);
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
