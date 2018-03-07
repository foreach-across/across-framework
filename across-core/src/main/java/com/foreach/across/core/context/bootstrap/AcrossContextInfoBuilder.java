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

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossContextConfigurationModule;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.*;

/**
 * Responsible for building the {@link AcrossContextInfo} for a configured {@link AcrossContext}.
 * The former will build the actual configurations that make up the context and modules, the
 * beans that should be created, installers to execute etc.
 *
 * @author Arne Vandamme
 * @since 4.0.0
 */
@Slf4j
class AcrossContextInfoBuilder
{
	public ConfigurableAcrossContextInfo build( AcrossContext acrossContext, MetadataReaderFactory metadataReaderFactory ) {
		checkUniqueModuleNames( acrossContext.getModules() );

		ConfigurableAcrossContextInfo contextInfo = new ConfigurableAcrossContextInfo( acrossContext );

		ModuleBootstrapOrderBuilder moduleBootstrapOrderBuilder = new ModuleBootstrapOrderBuilder();
		moduleBootstrapOrderBuilder.setDependencyResolver( acrossContext.getModuleDependencyResolver() );
		moduleBootstrapOrderBuilder.setSourceModules( acrossContext.getModules() );

		Collection<AcrossModuleInfo> configured = new LinkedList<>();

		int row = 1;
		for ( AcrossModule module : moduleBootstrapOrderBuilder.getOrderedModules() ) {
			ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( contextInfo, module, row++ );
			configured.add( moduleInfo );
		}

		configured.add(
				new ConfigurableAcrossModuleInfo( contextInfo, new AcrossContextConfigurationModule( AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE ),
				                                  row )
		);

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

		contextInfo.setBootstrapConfiguration( new AcrossModuleConfigurationBuilder().createBootstrapConfiguration( contextInfo, metadataReaderFactory ) );

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

	private void checkUniqueModuleNames( Collection<AcrossModule> modules ) {
		Set<String> moduleNames = new HashSet<>();

		for ( AcrossModule module : modules ) {
			if ( moduleNames.contains( module.getName() ) ) {
				throw new AcrossConfigurationException(
						"Each module must have a unique name, duplicate found for " + module.getName() );
			}

			moduleNames.add( module.getName() );
		}
	}

}
