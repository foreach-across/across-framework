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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ModuleConfigurationSet;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.context.installers.ClassPathScanningInstallerProvider;
import com.foreach.across.core.context.installers.InstallerSetBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Responsible for creating the actual module configuration, bean definitions that need
 * to be added in the scope of that module.
 *
 * @author Arne Vandamme
 * @since 4.0.0
 */
class AcrossModuleConfigurationBuilder
{
	public AcrossBootstrapConfig createBootstrapConfiguration( ConfigurableAcrossContextInfo contextInfo, MetadataReaderFactory metadataReaderFactory ) {
		List<ModuleBootstrapConfig> configs = new LinkedList<>();

		ApplicationContext applicationContext = contextInfo.getApplicationContext();
		//MetadataReaderFactory metadataReaderFactory = applicationContext.getBean( SharedMetadataReaderFactory.BEAN_NAME, MetadataReaderFactory.class );

		ClassPathScanningInstallerProvider installerProvider = new ClassPathScanningInstallerProvider( applicationContext, metadataReaderFactory );

		//BeanFilter defaultExposeFilter = buildDefaultExposeFilter( applicationContext.getClassLoader() );

		for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
			AcrossModule module = moduleInfo.getModule();
			ModuleBootstrapConfig config = new ModuleBootstrapConfig( moduleInfo );
			//config.setExposeFilter( new BeanFilterComposite( defaultExposeFilter, module.getExposeFilter() ) );
			//config.setExposeTransformer( module.getExposeTransformer() );
			config.setInstallerSettings( module.getInstallerSettings() );
			config.getInstallers().addAll( buildInstallerSet( module, installerProvider ) );

			// Provide the current module beans
			/*ProvidedBeansMap providedSingletons = new ProvidedBeansMap();
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

			// context and modules should use the main configuration report bean name
			if ( contextInfo.getApplicationContext().containsBean( AUTO_CONFIGURATION_REPORT_BEAN_NAME ) ) {
				providedSingletons.put( AUTO_CONFIGURATION_REPORT_BEAN_NAME,
				                        contextInfo.getApplicationContext()
				                                   .getBean( AUTO_CONFIGURATION_REPORT_BEAN_NAME ) );
			}

			registerSettings( module, providedSingletons, false );
			*/

			// Provided singletons do not influence initial load
			//config.addApplicationContextConfigurer( true, new ProvidedBeansConfigurer( providedSingletons ) );

			//if ( !isContextModule( config ) ) {
			// Only add default configurations if not a core module
			config.addApplicationContextConfigurers( AcrossContextUtils.getApplicationContextConfigurers( contextInfo.getContext(), module ) );
			//}

			// create installer application context
			//config.addInstallerContextConfigurer( new ProvidedBeansConfigurer( providedSingletons ) );
			config.addInstallerContextConfigurers( contextInfo.getContext().getInstallerContextConfigurers() );
			config.addInstallerContextConfigurers( AcrossContextUtils.getInstallerContextConfigurers( module ) );

			// add the module configuration importer
			//config.addApplicationContextConfigurer( true, ModuleConfigurationImportSelector.class );
			configs.add( config );

			( (ConfigurableAcrossModuleInfo) moduleInfo ).setBootstrapConfiguration( config );
		}

		AcrossBootstrapConfig contextConfig = new AcrossBootstrapConfig(
				contextInfo.getContext(), configs, buildModuleConfigurationSet( contextInfo )
		);
		/*
		contextConfig.setExposeTransformer( contextInfo.getContext().getExposeTransformer() );

		bootstrapConfigurers = new ArrayList<>(
				BeanFactoryUtils.beansOfTypeIncludingAncestors(
						(ListableBeanFactory) applicationContext.getAutowireCapableBeanFactory(), AcrossBootstrapConfigurer.class
				).values()
		);
		bootstrapConfigurers.sort( AnnotationAwareOrderComparator.INSTANCE );
		bootstrapConfigurers.forEach( configurer -> configurer.configureContext( contextConfig ) );

		contextInfo.setBootstrapConfiguration( contextConfig );
		*/

		return contextConfig;
	}

	private ModuleConfigurationSet buildModuleConfigurationSet( AcrossContextInfo contextInfo ) {
		/*
		ApplicationContext applicationContext = contextInfo.getApplicationContext();
		MetadataReaderFactory metadataReaderFactory
				= applicationContext.getBean( SharedMetadataReaderFactory.BEAN_NAME, MetadataReaderFactory.class );

		Set<String> basePackages = new LinkedHashSet<>();

		contextInfo.getModules()
		           .stream()
		           .filter( AcrossModuleInfo::isEnabled )
		           .forEach( acrossModuleInfo -> Collections.addAll(
				           basePackages, acrossModuleInfo.getModule().getModuleConfigurationScanPackages()
		                     )
		           );

		Collections.addAll( basePackages, contextInfo.getContext().getModuleConfigurationScanPackages() );

		return new ClassPathScanningModuleConfigurationProvider( applicationContext, metadataReaderFactory )
				.scan( basePackages.toArray( new String[basePackages.size()] ) );*/
		return new ModuleConfigurationSet();
	}

	private Collection<Object> buildInstallerSet( AcrossModule module, ClassPathScanningInstallerProvider installerProvider ) {
		InstallerSetBuilder installerSetBuilder = new InstallerSetBuilder( installerProvider );
		installerSetBuilder.add( module.getInstallers() );
		installerSetBuilder.scan( module.getInstallerScanPackages() );

		return Arrays.asList( installerSetBuilder.build() );
	}
}
