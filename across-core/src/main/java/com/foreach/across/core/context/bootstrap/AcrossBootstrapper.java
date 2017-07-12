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

package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.*;
import com.foreach.across.core.context.beans.PrimarySingletonBean;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.beans.SingletonBean;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.ProvidedBeansConfigurer;
import com.foreach.across.core.context.info.*;
import com.foreach.across.core.context.installers.InstallerSetBuilder;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.registry.DefaultAcrossContextBeanRegistry;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.filters.NamedBeanFilter;
import com.foreach.across.core.installers.AcrossBootstrapInstallerRegistry;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Takes care of bootstrapping an entire across context.
 */
public class AcrossBootstrapper
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossBootstrapper.class );

	private final AcrossContext context;
	private BootstrapApplicationContextFactory applicationContextFactory;

	private final Stack<ConfigurableApplicationContext> createdApplicationContexts = new Stack<>();
	private Throwable bootstrapEventError;

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
		try {
			bootstrapEventError = null;

			checkBootstrapIsPossible();

			ConfigurableAcrossContextInfo contextInfo = buildContextAndModuleInfo();
			Collection<AcrossModuleInfo> modulesInOrder = contextInfo.getModules();

			LOG.info( "Bootstrapping {} modules in the following order:", modulesInOrder.size() );
			for ( AcrossModuleInfo moduleInfo : modulesInOrder ) {
				LOG.info( "{} - {} [resources: {}]: {}", moduleInfo.getIndex(), moduleInfo.getName(),
				          moduleInfo.getResourcesKey(), moduleInfo.getModule().getClass() );
			}

			runModuleBootstrapperCustomizations( modulesInOrder );

			AcrossApplicationContextHolder root = createRootContext( contextInfo );
			AcrossConfigurableApplicationContext rootContext = root.getApplicationContext();

			createdApplicationContexts.push( rootContext );

			AcrossBootstrapConfig contextBoostrapConfig = createBootstrapConfiguration( contextInfo );
			prepareForBootstrap( contextInfo );

			BootstrapLockManager bootstrapLockManager = new BootstrapLockManager( contextInfo );

			AcrossEventPublisher eventPublisher = rootContext.getBean( AcrossEventPublisher.class );
			if ( context.isFailBootstrapOnEventPublicationErrors() ) {
				eventPublisher.addErrorHandler( new BootstrapEventErrorHandler() );
			}

			ModuleConfigurationSet moduleConfigurationSet = contextBoostrapConfig.getModuleConfigurationSet();

			try {
				AcrossBootstrapInstallerRegistry installerRegistry =
						new AcrossBootstrapInstallerRegistry(
								contextInfo.getBootstrapConfiguration(),
								bootstrapLockManager,
								applicationContextFactory
						);

				// Run installers that don't need anything bootstrapped
				installerRegistry.runInstallers( InstallerPhase.BeforeContextBootstrap );

				boolean pushExposedToParentContext = shouldPushExposedBeansToParent( contextInfo );
				ExposedContextBeanRegistry exposedBeanRegistry = new ExposedContextBeanRegistry(
						AcrossContextUtils.getBeanRegistry( contextInfo ),
						rootContext.getBeanFactory(),
						contextInfo.getBootstrapConfiguration().getExposeTransformer()
				);

				exposedBeanRegistry.add( AcrossContextInfo.BEAN );

				for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
					ConfigurableAcrossModuleInfo configurableAcrossModuleInfo =
							(ConfigurableAcrossModuleInfo) moduleInfo;
					ModuleBootstrapConfig config = moduleInfo.getBootstrapConfiguration();

					// Add scanned (or edited) module configurations
					config.addApplicationContextConfigurer(
							moduleConfigurationSet.getAnnotatedClasses( moduleInfo.getName() )
					);

					LOG.debug( "Bootstrapping {} module", config.getModuleName() );

					configurableAcrossModuleInfo.setBootstrapStatus( ModuleBootstrapStatus.BootstrapBusy );

					eventPublisher.publish( new AcrossModuleBeforeBootstrapEvent( contextInfo, moduleInfo ) );

					// Run installers before bootstrapping this particular module
					installerRegistry.runInstallersForModule( moduleInfo.getName(),
					                                          InstallerPhase.BeforeModuleBootstrap );

					// Create the module context
					AcrossConfigurableApplicationContext child =
							applicationContextFactory.createApplicationContext( context, config, root );

					AcrossApplicationContextHolder moduleApplicationContext = new AcrossApplicationContextHolder( child,
					                                                                                              root );
					AcrossContextUtils.setAcrossApplicationContextHolder( config.getModule(),
					                                                      moduleApplicationContext );

					applicationContextFactory.loadApplicationContext( context, config, moduleApplicationContext );

					// Bootstrap the module
					config.getModule().bootstrap();

					configurableAcrossModuleInfo.setBootstrapStatus( ModuleBootstrapStatus.Bootstrapped );

					// Send event that this module has bootstrapped
					eventPublisher.publish( new AcrossModuleBootstrappedEvent( moduleInfo ) );

					// Run installers after module itself has bootstrapped
					installerRegistry.runInstallersForModule( moduleInfo.getName(),
					                                          InstallerPhase.AfterModuleBootstrap );

					// Copy the beans to the parent context
					exposeBeans( configurableAcrossModuleInfo, config.getExposeFilter(), config.getExposeTransformer(),
					             rootContext );

					if ( pushExposedToParentContext ) {
						exposedBeanRegistry.addAll( configurableAcrossModuleInfo.getExposedBeanDefinitions() );
					}

					AcrossContextUtils.autoRegisterEventHandlers( child, eventPublisher );

					failOnEventErrors();
				}

				LOG.info( "Bootstrapping {} modules - finished", modulesInOrder.size() );

				if ( pushExposedToParentContext ) {
					pushExposedBeansToParent( exposedBeanRegistry, rootContext.getParent() );
				}

				// Refresh beans
				AcrossContextUtils.refreshBeans( context );

				contextInfo.setBootstrapped( true );

				// Bootstrapping done, run installers that require context bootstrap finished
				installerRegistry.runInstallers( InstallerPhase.AfterContextBootstrap );

				// Destroy the installer contexts
				installerRegistry.destroy();
			}
			finally {
				// Safe guard - ensure bootstrap released
				bootstrapLockManager.ensureUnlocked();
			}

			// Bootstrap finished - publish the event
			eventPublisher.publish( new AcrossContextBootstrappedEvent( contextInfo ) );

			failOnEventErrors();

			createdApplicationContexts.clear();
		}
		catch ( RuntimeException e ) {
			LOG.debug( "Exception during bootstrapping, destroying all created ApplicationContext instances" );

			destroyAllCreatedApplicationContexts();

			throw new AcrossException( "Across bootstrap failed", e );
		}
	}

	private void failOnEventErrors() {
		if ( context.isFailBootstrapOnEventPublicationErrors() && bootstrapEventError != null ) {
			throw new RuntimeException( bootstrapEventError );
		}
	}

	private void destroyAllCreatedApplicationContexts() {
		while ( !createdApplicationContexts.isEmpty() ) {
			try {
				createdApplicationContexts.pop().close();
			}
			catch ( Exception e ) {
				/*Ignore exception*/
				LOG.trace( "Exception destroying ApplicationContext", e );
			}
		}
	}

	private boolean shouldPushExposedBeansToParent( AcrossContextInfo contextInfo ) {
		ApplicationContext applicationContext = contextInfo.getApplicationContext();

		if ( applicationContext.getParent() != null && !( applicationContext
				.getParent() instanceof ConfigurableApplicationContext ) ) {
			LOG.warn(
					"Unable to push the exposed beans to the parent ApplicationContext - requires a ConfigurableApplicationContext" );
		}

		return applicationContext.getParent() != null;
	}

	private void pushExposedBeansToParent( ExposedContextBeanRegistry exposedContextBeanRegistry,
	                                       ApplicationContext applicationContext ) {
		if ( !exposedContextBeanRegistry.isEmpty() ) {
			ConfigurableApplicationContext currentApplicationContext =
					(ConfigurableApplicationContext) applicationContext;
			ConfigurableListableBeanFactory currentBeanFactory = currentApplicationContext.getBeanFactory();

			ConfigurableListableBeanFactory beanFactory = currentBeanFactory;

			// If the direct parent does not handle exposed beans, check if another context already introduced
			// a supporting context higher up
			if ( !( beanFactory instanceof AcrossListableBeanFactory ) && currentApplicationContext
					.getParent() != null ) {
				ApplicationContext parent = currentApplicationContext.getParent();

				if ( parent instanceof ConfigurableApplicationContext ) {
					beanFactory = ( (ConfigurableApplicationContext) parent ).getBeanFactory();
				}
			}

			// Make sure the parent can handle exposed beans - if not, introduce a supporting BeanFactory in the hierarchy
			if ( !( beanFactory instanceof AcrossListableBeanFactory ) ) {
				AcrossConfigurableApplicationContext parentApplicationContext =
						applicationContextFactory.createApplicationContext();
				parentApplicationContext.refresh();
				parentApplicationContext.start();

				ConfigurableListableBeanFactory parentBeanFactory = parentApplicationContext.getBeanFactory();

				parentBeanFactory.setParentBeanFactory( currentBeanFactory.getParentBeanFactory() );
				parentApplicationContext.setParent( currentApplicationContext.getParent() );

				currentApplicationContext.setParent( parentApplicationContext );
				currentBeanFactory.setParentBeanFactory( parentBeanFactory );

				beanFactory = parentApplicationContext.getBeanFactory();
			}

			exposedContextBeanRegistry.copyTo( beanFactory );
		}
	}

	private void exposeBeans( ConfigurableAcrossModuleInfo acrossModuleInfo,
	                          BeanFilter exposeFilter,
	                          ExposedBeanDefinitionTransformer exposeTransformer,
	                          AcrossConfigurableApplicationContext parentContext ) {
		BeanFilter exposeFilterToApply = exposeFilter;

		AcrossListableBeanFactory moduleBeanFactory = AcrossContextUtils.getBeanFactory(
				acrossModuleInfo );

		String[] exposedBeanNames = moduleBeanFactory.getExposedBeanNames();

		if ( exposedBeanNames.length > 0 ) {
			exposeFilterToApply = new BeanFilterComposite(
					exposeFilter,
					new NamedBeanFilter( exposedBeanNames )
			);
		}

		ExposedModuleBeanRegistry exposedBeanRegistry = new ExposedModuleBeanRegistry(
				AcrossContextUtils.getBeanRegistry( acrossModuleInfo.getContextInfo() ),
				acrossModuleInfo,
				(AbstractApplicationContext) acrossModuleInfo.getApplicationContext(),
				exposeFilterToApply,
				exposeTransformer
		);

		exposedBeanRegistry.copyTo( parentContext.getBeanFactory() );

		acrossModuleInfo.setExposedBeanRegistry( exposedBeanRegistry );
	}

	private ConfigurableAcrossContextInfo buildContextAndModuleInfo() {
		ConfigurableAcrossContextInfo contextInfo = new ConfigurableAcrossContextInfo( context );
		ModuleBootstrapOrderBuilder moduleBootstrapOrderBuilder = new ModuleBootstrapOrderBuilder();
		moduleBootstrapOrderBuilder.setDependencyResolver( context.getModuleDependencyResolver() );
		moduleBootstrapOrderBuilder.setSourceModules( context.getModules() );

		Collection<AcrossModuleInfo> configured = new LinkedList<>();

		int row = 1;
		for ( AcrossModule module : moduleBootstrapOrderBuilder.getOrderedModules() ) {
			ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( contextInfo, module, row++ );
			//moduleInfo.setSettings( createModuleSettings( module.getClass() ) );

			configured.add( moduleInfo );
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
	private AcrossBootstrapConfig createBootstrapConfiguration( ConfigurableAcrossContextInfo contextInfo ) {
		List<ModuleBootstrapConfig> configs = new LinkedList<>();

		for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
			AcrossModule module = moduleInfo.getModule();
			ModuleBootstrapConfig config = new ModuleBootstrapConfig( module, moduleInfo.getIndex() );
			config.setExposeFilter( module.getExposeFilter() );
			config.setExposeTransformer( module.getExposeTransformer() );
			config.setInstallerSettings( module.getInstallerSettings() );
			config.getInstallers().addAll( buildInstallerSet( module ) );

			// Provide the current module beans
			ProvidedBeansMap providedSingletons = new ProvidedBeansMap();
			providedSingletons.put( AcrossModule.CURRENT_MODULE + "Info",
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

			registerSettings( module, providedSingletons, false );

			config.addApplicationContextConfigurer( new ProvidedBeansConfigurer( providedSingletons ) );
			config.addApplicationContextConfigurers(
					AcrossContextUtils.getApplicationContextConfigurers( context, module )
			);

			// create installer application context
			config.addInstallerContextConfigurer( new ProvidedBeansConfigurer( providedSingletons ) );
			config.addInstallerContextConfigurers( contextInfo.getContext().getInstallerContextConfigurers() );
			config.addInstallerContextConfigurers( AcrossContextUtils.getInstallerContextConfigurers( module ) );

			configs.add( config );

			( (ConfigurableAcrossModuleInfo) moduleInfo ).setBootstrapConfiguration( config );
		}

		AcrossBootstrapConfig contextConfig = new AcrossBootstrapConfig(
				contextInfo.getContext(), configs, buildModuleConfigurationSet( contextInfo )
		);
		contextConfig.setExposeTransformer( contextInfo.getContext().getExposeTransformer() );

		contextInfo.setBootstrapConfiguration( contextConfig );

		return contextConfig;
	}

	private void registerSettings( AcrossModule module, ProvidedBeansMap beansMap, boolean compatibility ) {
		String settingsClassName = ClassUtils.getUserClass( module.getClass() ).getName() + "Settings";

		try {
			Class settingsClass = Class.forName( settingsClassName );

			if ( !settingsClass.isInterface() && !Modifier.isAbstract( settingsClass.getModifiers() ) ) {
				if ( !compatibility ) {
					// Register settings as bean in the module application context
					GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
					beanDefinition.setBeanClass( settingsClass );
					beanDefinition.setPrimary( true );
					beanDefinition.addQualifier(
							new AutowireCandidateQualifier( Module.class.getName(), AcrossModule.CURRENT_MODULE )
					);

					beansMap.put( AcrossModule.CURRENT_MODULE + "Settings", beanDefinition );
				}
				else if ( AcrossModuleSettings.class.isAssignableFrom( settingsClass ) ) {
					// If this is an old settings class, register it in the parent context as well,
					// note that this means the bean will be wired in a different context than in the module
					GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
					beanDefinition.setBeanClass( settingsClass );
					beanDefinition.setPrimary( false );
					beanDefinition.setLazyInit( true );
					beanDefinition.addQualifier(
							new AutowireCandidateQualifier( Module.class.getName(), module.getName() )
					);

					beansMap.put( settingsClassName, beanDefinition );
				}
			}
		}
		catch ( ClassNotFoundException ignore ) {
		}
	}

	private Collection<Object> buildInstallerSet( AcrossModule module ) {
		InstallerSetBuilder installerSetBuilder = new InstallerSetBuilder();
		installerSetBuilder.add( module.getInstallers() );
		installerSetBuilder.scan( module.getInstallerScanPackages() );

		return Arrays.asList( installerSetBuilder.build() );
	}

	private ModuleConfigurationSet buildModuleConfigurationSet( AcrossContextInfo contextInfo ) {
		Set<String> basePackages = new LinkedHashSet<>();

		contextInfo.getModules()
		           .stream()
		           .filter( AcrossModuleInfo::isEnabled )
		           .forEach( acrossModuleInfo -> Collections.addAll(
				           basePackages, acrossModuleInfo.getModule().getModuleConfigurationScanPackages()
		                     )
		           );

		Collections.addAll( basePackages, contextInfo.getContext().getModuleConfigurationScanPackages() );

		return new ClassPathScanningModuleConfigurationProvider()
				.scan( basePackages.toArray( new String[basePackages.size()] ) );
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

	private AcrossApplicationContextHolder createRootContext( ConfigurableAcrossContextInfo contextInfo ) {
		AcrossConfigurableApplicationContext rootApplicationContext =
				applicationContextFactory.createApplicationContext( context,
				                                                    context.getParentApplicationContext() );

		ProvidedBeansMap providedBeans = new ProvidedBeansMap();

		// Create the AcrossContextBeanRegistry
		AcrossContextBeanRegistry contextBeanRegistry = new DefaultAcrossContextBeanRegistry( contextInfo );
		providedBeans.put( contextBeanRegistry.getFactoryName(),
		                   new PrimarySingletonBean(
				                   new DefaultAcrossContextBeanRegistry( contextInfo ),
				                   new AutowireCandidateQualifier( Qualifier.class.getName(),
				                                                   AcrossContextBeanRegistry.BEAN )
		                   ) );

		// Put the context and its info as fixed singletons
		providedBeans.put( AcrossContext.BEAN, new PrimarySingletonBean( context ) );
		providedBeans.put( AcrossContextInfo.BEAN, new PrimarySingletonBean( contextInfo ) );

		// Put the module info as singletons in the context
		for ( AcrossModuleInfo moduleInfo : contextInfo.getConfiguredModules() ) {
			// Create the module instances as primary beans so they do not clash with modules
			// configured as beans in a parent application context
			providedBeans.put( "across.module." + moduleInfo.getName(),
			                   new PrimarySingletonBean(
					                   moduleInfo.getModule(),
					                   new AutowireCandidateQualifier( Module.class.getName(),
					                                                   moduleInfo.getName() )
			                   )
			);
			providedBeans.put( moduleInfo.getName(),
			                   new SingletonBean(
					                   moduleInfo,
					                   new AutowireCandidateQualifier( Module.class.getName(),
					                                                   moduleInfo.getName() )
			                   )
			);

			registerSettings( moduleInfo.getModule(), providedBeans, true );
		}

		context.addApplicationContextConfigurer( new ProvidedBeansConfigurer( providedBeans ),
		                                         ConfigurerScope.CONTEXT_ONLY );

		AcrossApplicationContextHolder root = new AcrossApplicationContextHolder( rootApplicationContext );
		AcrossContextUtils.setAcrossApplicationContextHolder( context, root );

		applicationContextFactory.loadApplicationContext( context, root );

		return root;
	}

	class BootstrapEventErrorHandler implements IPublicationErrorHandler
	{
		@Override
		public void handleError( PublicationError error ) {
			bootstrapEventError = error.getCause();
		}
	}
}
