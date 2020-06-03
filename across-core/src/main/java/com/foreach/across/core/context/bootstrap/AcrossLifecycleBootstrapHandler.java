/*
 * Copyright 2019 the original author or authors
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
import com.foreach.across.core.AcrossContextConfigurationModule;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.context.module.*;
import com.foreach.across.core.events.AcrossLifecycleEvent;
import com.foreach.across.core.installers.InstallerPhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer.CONTEXT_INFRASTRUCTURE_MODULE;
import static com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE;

/**
 * Responsible for bootstrapping an Across context. Builds the module configurations
 * and sets up all required infrastructure, then starts all modules in order.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public final class AcrossLifecycleBootstrapHandler
{
	private final AcrossContext acrossContext;
	private final AcrossBootstrapInfrastructure infrastructure;

	private AcrossModuleBootstrapConfigurationSet moduleBootstrapConfigurations;
	private AcrossContextInfo contextInfo;

	public AcrossLifecycleBootstrapHandler( AcrossContext acrossContext ) {
		this.acrossContext = acrossContext;
		this.infrastructure = new AcrossBootstrapInfrastructure( acrossContext );
	}

	/**
	 * Performs the actual bootstrapping.
	 */
	public void bootstrap() {
		buildModuleBootstrapConfigurationSet();
		buildContextAndModuleInfo();

		printBootstrapSummary();

		configureApplicationContextFactory();

		try {
			LOG.debug( "" );
			LOG.debug( "--- Executing installers: BeforeContextBootstrap" );
			moduleBootstrapConfigurations.forEach( cfg -> runInstallers( InstallerPhase.BeforeContextBootstrap, cfg ) );

			LOG.info( "" );
			LOG.info( "--- Starting module bootstrap" );

			contextInfo.getModules().forEach( moduleInfo -> {
				printModuleSummary( moduleInfo, true );

				LOG.info( "" );
				LOG.info( "<<< {} - {} {}", String.format( "%2s", moduleInfo.getIndex() ), moduleInfo.getName(), moduleInfo.getVersionInfo().getVersion() );
			} );

			LOG.info( "" );
			LOG.info( "--- Module bootstrap finished: {} modules started", contextInfo.getModules().size() );
			LOG.info( "" );

			LOG.debug( "--- Executing installers: AfterContextBootstrap" );
			moduleBootstrapConfigurations.forEach( cfg -> runInstallers( InstallerPhase.AfterContextBootstrap, cfg ) );

		}
		finally {

		}

		// start module bootstrap

		// refresh beans

		// cleanup
	}

	private void printBootstrapSummary() {
		Collection<AcrossModuleInfo> modulesInOrder = contextInfo.getModules();

		LOG.info( "---" );
		LOG.info( "AcrossContext: {} ({})", acrossContext.getDisplayName(), acrossContext.getId() );
		LOG.info( "Bootstrapping {} modules in the following order:", modulesInOrder.size() );
		modulesInOrder.forEach( moduleInfo -> printModuleSummary( moduleInfo, false ) );
		LOG.info( "---" );
	}

	private void printModuleSummary( AcrossModuleInfo moduleInfo, boolean detailed ) {
		if ( detailed ) {
			LOG.info( "" );
		}

		final String modulePrefix = detailed ? ">>> " : "";
		LOG.info( "{}{} - {} {} [resources: {}]", modulePrefix, String.format( "%2s", moduleInfo.getIndex() ), moduleInfo.getName(),
		          moduleInfo.getVersionInfo().getVersion(), moduleInfo.getResourcesKey() );

		Collection<AcrossModuleConfiguration> extensions = moduleInfo.getModuleBootstrapConfiguration().getExtensions();

		if ( !extensions.isEmpty() ) {
			final String extensionPrefix = detailed ? "         - " : "   + ";

			if ( detailed ) {
				LOG.info( "" );
				LOG.info( "         Module extensions:" );
			}

			extensions.forEach( extension -> {
				AcrossModuleDescriptor extensionModuleDescriptor = extension.getModuleDescriptor();
				LOG.info( "{} {} {} [resources: {}]", extensionPrefix, extensionModuleDescriptor.getModuleName(),
				          extensionModuleDescriptor.getVersionInfo().getVersion(), extensionModuleDescriptor.getResourcesKey() );
			} );

		}

		if ( detailed ) {
			LOG.info( "" );
		}
	}

	private void buildContextAndModuleInfo() {
		ConfigurableAcrossContextInfo contextInfo = new ConfigurableAcrossContextInfo( acrossContext );

		int row = 1;

		List<AcrossModuleInfo> moduleInfoList = new ArrayList<>( moduleBootstrapConfigurations.size() );
		for ( AcrossModuleBootstrapConfiguration moduleBootstrapConfiguration : moduleBootstrapConfigurations.getConfigurationsInOrder() ) {
			moduleInfoList.add( new ConfigurableAcrossModuleInfo( contextInfo, moduleBootstrapConfiguration, row++ ) );
		}

		contextInfo.setConfiguredModules( moduleInfoList );

		this.contextInfo = contextInfo;
	}

	private void configureApplicationContextFactory() {

	}

	private AcrossBootstrapInfrastructure createBootstrapInfrastructure() {
		return null;
	}

	private void runInstallers( InstallerPhase installerPhase, AcrossModuleBootstrapConfiguration configuration ) {
	}

	private void publishLifecycleEvent( AcrossLifecycleEvent event ) {

	}

	private void buildModuleBootstrapConfigurationSet() {
		Collection<AcrossModuleDescriptor> moduleDescriptors = new ArrayList<>( new AcrossModuleDescriptorSetBuilder()
				                                                                        .dependencyResolver( acrossContext.getModuleDependencyResolver() )
				                                                                        .build( acrossContext.getModules() ) );

		// add AcrossContextInfrastructureModule
		moduleDescriptors.add(
				AcrossModuleDescriptor.from( new AcrossContextConfigurationModule( CONTEXT_INFRASTRUCTURE_MODULE ) )
				                      .toBuilder()
				                      .orderInModuleRole( Ordered.HIGHEST_PRECEDENCE + 1000 )
				                      .moduleRole( AcrossModuleRole.INFRASTRUCTURE )
				                      .build()
		);

		// add AcrossContextPostProcessorModule
		moduleDescriptors.add(
				AcrossModuleDescriptor.from( new AcrossContextConfigurationModule( CONTEXT_POSTPROCESSOR_MODULE ) )
				                      .toBuilder()
				                      .orderInModuleRole( Ordered.LOWEST_PRECEDENCE - 1000 )
				                      .moduleRole( AcrossModuleRole.POSTPROCESSOR )
				                      .build()
		);

		moduleBootstrapConfigurations = AcrossModuleBootstrapConfigurationSet.create( moduleDescriptors );
	}
}

