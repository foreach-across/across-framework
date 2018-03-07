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
import com.foreach.across.core.context.*;
import com.foreach.across.core.context.info.*;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.registry.DefaultAcrossContextBeanRegistry;
import com.foreach.across.core.installers.AcrossBootstrapInstallerRegistry;
import com.foreach.across.core.installers.InstallerPhase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Collection;

import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME;

/**
 * Custom extension of the default {@link ConfigurationClassPostProcessor} that supports configuring Across modules
 * inside the {@link org.springframework.context.ApplicationContext}. After initial configuration is done, an
 * {@link com.foreach.across.core.AcrossContext} bean is eagerly instantiated and used as a basis for additional
 * configuration registrations where all resulting bean definitions will be scoped to the module being bootstrapped.
 *
 * @author Arne Vandamme
 * @since 4.0.0
 */
@Slf4j
public class AcrossConfigurationClassPostProcessor extends ConfigurationClassPostProcessor
{
	private static final String MODULE_REGISTRAR = AcrossModuleConfigurationRegistrar.class.getName();

	private AcrossConfigurableApplicationContext applicationContext;

	@Override
	public void setResourceLoader( ResourceLoader resourceLoader ) {
		super.setResourceLoader( resourceLoader );

		this.applicationContext = (AcrossConfigurableApplicationContext) resourceLoader;
	}

	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) {
		super.postProcessBeanFactory( beanFactory );

		if ( beanFactory.containsBeanDefinition( AcrossContext.BEAN ) ) {
			registerAcrossContextConfiguration( (AcrossListableBeanFactory) beanFactory );
		}
	}

	private void registerAcrossContextConfiguration( AcrossListableBeanFactory beanFactory ) {
		AcrossContext acrossContext = beanFactory.getBean( AcrossContext.BEAN, AcrossContext.class );
		AcrossContextUtils.setAcrossApplicationContextHolder( acrossContext, new AcrossApplicationContextHolder( applicationContext ) );

		ConfigurableAcrossContextInfo contextInfo = new AcrossContextInfoBuilder()
				.build(
						acrossContext,
						beanFactory.getBean( SharedMetadataReaderFactory.BEAN_NAME, MetadataReaderFactory.class )
				);

		beanFactory.registerBeanDefinition( AcrossContextInfo.BEAN,
		                                    BeanDefinitionBuilder.genericBeanDefinition( AcrossContextInfo.class ).getBeanDefinition() );
		beanFactory.registerSingleton( AcrossContextInfo.BEAN, contextInfo );

		AcrossContextBeanRegistry contextBeanRegistry = new DefaultAcrossContextBeanRegistry( contextInfo );
		beanFactory.registerBeanDefinition( AcrossContextBeanRegistry.BEAN,
		                                    BeanDefinitionBuilder.genericBeanDefinition( AcrossContextBeanRegistry.class ).getBeanDefinition() );
		beanFactory.registerSingleton( AcrossContextBeanRegistry.BEAN, contextBeanRegistry );

		Collection<AcrossModuleInfo> modulesInOrder = contextInfo.getModules();

		LOG.info( "---" );
		LOG.info( "AcrossContext: {} ({})", contextInfo.getDisplayName(), contextInfo.getId() );
		LOG.info( "Bootstrapping {} modules in the following order:", modulesInOrder.size() );
		for ( AcrossModuleInfo moduleInfo : modulesInOrder ) {
			LOG.info( "{} - {} [resources: {}]: {}", moduleInfo.getIndex(), moduleInfo.getName(),
			          moduleInfo.getResourcesKey(), moduleInfo.getModule().getClass() );
		}
		LOG.info( "---" );

		contextInfo.getModules().forEach( module -> {
			beanFactory.setModuleIndex( module.getIndex() );
			( (ConfigurableAcrossModuleInfo) module ).setBootstrapStatus( ModuleBootstrapStatus.BootstrapBusy );
			LOG.info( "Registering bean definitions for module {}", module.getName() );

			removeModuleConfigurationRegistrar( beanFactory );
			addModuleConfigurationRegistrar( beanFactory );

			super.processConfigBeanDefinitions( beanFactory );

			( (ConfigurableAcrossModuleInfo) module ).setBootstrapStatus( ModuleBootstrapStatus.Bootstrapped );
		} );

		removeModuleConfigurationRegistrar( beanFactory );

		beanFactory.setModuleIndex( beanFactory.getModuleIndex() + 1 );

		enhanceConfigurationClasses( beanFactory );

		BootstrapLockManager bootstrapLockManager = new BootstrapLockManager( contextInfo );
		try {
			AcrossBootstrapInstallerRegistry installerRegistry =
					new AcrossBootstrapInstallerRegistry(
							contextInfo.getBootstrapConfiguration(),
							bootstrapLockManager,
							new AnnotationConfigBootstrapApplicationContextFactory()
					);

			contextInfo.getModules().forEach( module -> installerRegistry.runInstallersForModule( module.getName(), InstallerPhase.BeforeContextBootstrap ) );
			contextInfo.getModules().forEach( module -> installerRegistry.runInstallersForModule( module.getName(), InstallerPhase.BeforeModuleBootstrap ) );
			contextInfo.getModules().forEach( module -> installerRegistry.runInstallersForModule( module.getName(), InstallerPhase.AfterModuleBootstrap ) );
			contextInfo.getModules().forEach( module -> installerRegistry.runInstallersForModule( module.getName(), InstallerPhase.AfterContextBootstrap ) );

			installerRegistry.destroy();
		}
		finally {
			bootstrapLockManager.ensureUnlocked();
		}
	}

	private void addModuleConfigurationRegistrar( AcrossListableBeanFactory beanFactory ) {
		beanFactory.registerBeanDefinition( MODULE_REGISTRAR, new AnnotatedGenericBeanDefinition( AcrossModuleConfigurationRegistrar.class ) );
	}

	private void removeModuleConfigurationRegistrar( AcrossListableBeanFactory beanFactory ) {
		if ( beanFactory.containsBeanDefinition( MODULE_REGISTRAR ) ) {
			beanFactory.removeBeanDefinition( MODULE_REGISTRAR );
		}
	}

	/**
	 * A {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that modifies the default {@link ConfigurationClassPostProcessor}
	 * to the custom {@link AcrossConfigurationClassPostProcessor} implementation.
	 */
	public static class Registrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered
	{
		@Override
		public int getOrder() {
			// Must happen before the ConfigurationClassPostProcessor is created
			return Ordered.HIGHEST_PRECEDENCE;
		}

		@Override
		public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
		}

		@Override
		public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
			try {
				RootBeanDefinition beanDefinition = (RootBeanDefinition) registry.getBeanDefinition( CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME );
				beanDefinition.setBeanClass( AcrossConfigurationClassPostProcessor.class );
			}
			catch ( NoSuchBeanDefinitionException ex ) {
				LOG.error( "Unable to modify the ConfigurationClassPostProcessor for Across", ex );
			}
		}
	}
}
