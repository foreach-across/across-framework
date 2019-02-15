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
import com.foreach.across.core.context.module.AcrossModuleBootstrapConfiguration;
import com.foreach.across.core.context.module.AcrossModuleBootstrapConfigurationSet;
import com.foreach.across.core.context.module.AcrossModuleDescriptor;
import com.foreach.across.core.context.module.AcrossModuleDescriptorSetBuilder;
import com.foreach.across.core.events.AcrossLifecycleEvent;
import com.foreach.across.core.installers.InstallerPhase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collection;

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
public final class AcrossLifecycleBootstrapHandler
{
	private final AcrossContext acrossContext;
	private final AcrossBootstrapInfrastructure infrastructure;

	private AcrossModuleBootstrapConfigurationSet moduleBootstrapConfigurations;

	public AcrossLifecycleBootstrapHandler( AcrossContext acrossContext ) {
		this.acrossContext = acrossContext;
		this.infrastructure = new AcrossBootstrapInfrastructure( acrossContext );
	}

	/**
	 * Performs the actual bootstrapping.
	 */
	public void bootstrap() {
		buildModuleBootstrapConfigurationSet();

		LOG.info( "---" );
		LOG.info( "AcrossContext: {} ({})", acrossContext.getDisplayName(), acrossContext.getId() );
		LOG.info( "Bootstrapping {} modules in the following order:", moduleBootstrapConfigurations.size() );
		/*for ( AcrossModuleInfo moduleInfo : modulesInOrder ) {
			LOG.info( "{} - {} [resources: {}]: {}", moduleInfo.getIndex(), moduleInfo.getName(),
			          moduleInfo.getResourcesKey(), moduleInfo.getModule().getClass() );
		}*/
		LOG.info( "---" );

		configureApplicationContextFactory();

		try {
			LOG.trace( "Executing installers: BeforeContextBootstrap" );
			moduleBootstrapConfigurations.forEach( cfg -> runInstallers( InstallerPhase.BeforeContextBootstrap, cfg ) );

			LOG.info( "" );
			LOG.info( "--- Starting module bootstrap" );
			LOG.info( "" );

			moduleBootstrapConfigurations.forEach( moduleConfiguration -> {
				/*LOG.info( "{} - {} [resources: {}]: {}", moduleInfo.getIndex(), moduleInfo.getName(),
				          moduleInfo.getResourcesKey(), moduleInfo.getModule().getClass() );*/

				LOG.info( "" );
			} );

			//LOG.info( "--- Module bootstrap finished: {} modules started", contextInfo.getModules().size() );
			LOG.info( "" );

			LOG.trace( "Executing installers: AfterContextBootstrap" );
			moduleBootstrapConfigurations.forEach( cfg -> runInstallers( InstallerPhase.AfterContextBootstrap, cfg ) );


		}
		finally {

		}

		// start module bootstrap

		// refresh beans

		// cleanup
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

