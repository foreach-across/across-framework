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
package com.foreach.across.core.context.module;

import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.module.AcrossModuleDependencySorter.DependencySpec;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.installers.InstallerReference;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("AcrossModuleBootstrapConfiguration creation")
@ExtendWith(MockitoExtension.class)
class TestAcrossModuleBootstrapConfiguration
{
	@Mock
	private ApplicationContextConfigurer appConfigurer;

	@Mock
	private ApplicationContextConfigurer installerConfigurer;

	private BeanFilter exposeFilter = BeanFilter.empty();

	@Mock
	private InstallerSettings installerSettings;

	@Mock
	private ExposedBeanDefinitionTransformer exposeTransformer;

	@Test
	@DisplayName("from AcrossModuleDescriptor")
	void fromModuleDescriptor() {
		AcrossModuleDescriptor descriptor = AcrossModuleDescriptor
				.builder()
				.moduleName( "myModule" )
				.resourcesKey( "myModuleResources" )
				.applicationContextConfigurer( appConfigurer )
				.installerContextConfigurer( installerConfigurer )
				.installerScanPackage( "installerPackage" )
				.moduleConfigurationScanPackage( "moduleConfigurationPackage" )
				.installerSettings( installerSettings )
				.exposeFilter( exposeFilter )
				.exposeTransformer( exposeTransformer )
				.property( "one", 1 )
				.installer( InstallerReference.from( "SomeClassName" ) )
				.build();

		AcrossModuleBootstrapConfiguration configuration = AcrossModuleBootstrapConfiguration.from( descriptor );

		assertThat( configuration ).isNotNull();
		assertThat( configuration.getModuleDescriptor() ).isSameAs( descriptor );
		assertThat( configuration.getExtensions() ).isEmpty();
		assertThat( configuration.getApplicationContextConfigurers() ).containsExactly( appConfigurer );
		assertThat( configuration.getInstallerContextConfigurers() ).containsExactly( installerConfigurer );
		assertThat( configuration.getInstallerScanPackages() ).containsExactly( "installerPackage" );
		assertThat( configuration.getModuleConfigurationScanPackages() ).containsExactly( "moduleConfigurationPackage" );
		assertThat( configuration.getInstallerSettings() ).contains( installerSettings );
		assertThat( configuration.getExposeFilter() ).contains( exposeFilter );
		assertThat( configuration.getExposeTransformer() ).contains( exposeTransformer );
		assertThat( configuration.getProperties() ).containsEntry( "one", 1 ).hasSize( 1 );
		assertThat( configuration.getInstallers() ).containsExactly( InstallerReference.from( "SomeClassName" ) );
	}

	@Test
	@DisplayName("modify configuration")
	void modifyConfiguration() {
		AcrossModuleDescriptor descriptor = AcrossModuleDescriptor
				.builder()
				.moduleName( "myModule" )
				.resourcesKey( "myModuleResources" )
				.build();

		AcrossModuleBootstrapConfiguration configuration = AcrossModuleBootstrapConfiguration.from( descriptor );
		AcrossModuleBootstrapConfiguration extension = AcrossModuleBootstrapConfiguration.from( descriptor );
		assertThat( configuration.getModuleDescriptor() ).isSameAs( descriptor );

		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> configuration.addExtension( configuration ) );

		assertThat( configuration.addExtension( extension )
		                         .addApplicationContextConfigurers( appConfigurer )
		                         .addInstallerContextConfigurers( installerConfigurer )
		                         .addInstallerScanPackages( "installerPackage" )
		                         .addModuleConfigurationScanPackages( "moduleConfigurationPackage" )
		                         .setInstallerSettings( installerSettings )
		                         .addExposeFilter( exposeFilter )
		                         .addExposeTransformer( exposeTransformer )
		                         .addProperty( "one", 1 )
		                         .addInstallers( InstallerReference.from( "SomeClassName" ) ) )
				.isSameAs( configuration );

		assertThat( configuration.getExtensions() ).containsExactly( extension );
		assertThat( configuration.getApplicationContextConfigurers() ).containsExactly( appConfigurer );
		assertThat( configuration.getInstallerContextConfigurers() ).containsExactly( installerConfigurer );
		assertThat( configuration.getInstallerScanPackages() ).containsExactly( "installerPackage" );
		assertThat( configuration.getModuleConfigurationScanPackages() ).containsExactly( "moduleConfigurationPackage" );
		assertThat( configuration.getInstallerSettings() ).contains( installerSettings );
		assertThat( configuration.getExposeFilter() ).contains( exposeFilter );
		assertThat( configuration.getExposeTransformer() ).contains( exposeTransformer );
		assertThat( configuration.getProperties() ).containsEntry( "one", 1 ).hasSize( 1 );
		assertThat( configuration.getInstallers() ).containsExactly( InstallerReference.from( "SomeClassName" ) );

		assertThat( configuration.clearApplicationContextConfigurers()
		                         .clearInstallerContextConfigurers()
		                         .clearInstallerScanPackages()
		                         .clearModuleConfigurationScanPackages()
		                         .setInstallerSettings( null )
		                         .setExposeFilter( null )
		                         .setExposeTransformer( null )
		                         .clearProperties()
		                         .clearInstallers() )
				.isSameAs( configuration );

		assertThat( configuration.getExtensions() ).containsExactly( extension );
		assertThat( configuration.getApplicationContextConfigurers() ).isEmpty();
		assertThat( configuration.getInstallerContextConfigurers() ).isEmpty();
		assertThat( configuration.getInstallerScanPackages() ).isEmpty();
		assertThat( configuration.getModuleConfigurationScanPackages() ).isEmpty();
		assertThat( configuration.getInstallerSettings() ).isEmpty();
		assertThat( configuration.getExposeFilter() ).isEmpty();
		assertThat( configuration.getExposeTransformer() ).isEmpty();
		assertThat( configuration.getProperties() ).isEmpty();
		assertThat( configuration.getInstallers() ).isEmpty();
	}

	@Test
	@DisplayName("toDependencySpec: single configuration")
	void toDependencySpecForSingleModule() {
		DependencySpec dependencySpec = AcrossModuleBootstrapConfiguration
				.from( AcrossModuleDescriptor
						       .builder()
						       .moduleName( "myModule" )
						       .resourcesKey( "res" )
						       .moduleNameAlias( "aliasOne" )
						       .moduleNameAlias( "aliasTwo" )
						       .requiredModule( "One" )
						       .optionalModule( "Two" )
						       .optionalModule( "Three" )
						       .moduleRole( AcrossModuleRole.INFRASTRUCTURE )
						       .orderInModuleRole( 1000 )
						       .build() )
				.toDependencySpec();

		assertThat( dependencySpec ).isNotNull();
		assertThat( dependencySpec.getNames() ).containsExactly( "myModule", "aliasOne", "aliasTwo" );
		assertThat( dependencySpec.getRequiredDependencies() ).containsExactly( "One" );
		assertThat( dependencySpec.getOptionalDependencies() ).containsExactly( "Two", "Three" );
		assertThat( dependencySpec.getOrderInRole() ).isEqualTo( 1000 );
		assertThat( dependencySpec.getRole() ).isEqualTo( AcrossModuleRole.INFRASTRUCTURE );
	}

	@Test
	@DisplayName("toDependencySpec: with extensions")
	void toDependencySpecForExtensions() {
		AcrossModuleBootstrapConfiguration initial = AcrossModuleBootstrapConfiguration
				.from( AcrossModuleDescriptor
						       .builder()
						       .moduleName( "myModule" )
						       .resourcesKey( "res" )
						       .moduleNameAlias( "aliasOne" )
						       .moduleNameAlias( "aliasTwo" )
						       .requiredModule( "One" )
						       .optionalModule( "Two" )
						       .optionalModule( "Three" )
						       .moduleRole( AcrossModuleRole.INFRASTRUCTURE )
						       .orderInModuleRole( 1000 )
						       .build() );
		AcrossModuleBootstrapConfiguration extension = AcrossModuleBootstrapConfiguration
				.from( AcrossModuleDescriptor
						       .builder()
						       .moduleName( "extensionModule" )
						       .resourcesKey( "res" )
						       .moduleNameAlias( "aliasThree" )
						       .requiredModule( "One" )
						       .requiredModule( "Two" )
						       .optionalModule( "Two" )
						       .optionalModule( "Four" )
						       .moduleRole( AcrossModuleRole.POSTPROCESSOR )
						       .orderInModuleRole( 10 )
						       .build() );

		initial.addExtension( extension );

		DependencySpec dependencySpec = initial.toDependencySpec();

		assertThat( dependencySpec ).isNotNull();
		assertThat( dependencySpec.getNames() ).containsExactly( "myModule", "aliasOne", "aliasTwo", "extensionModule", "aliasThree" );
		assertThat( dependencySpec.getRequiredDependencies() ).containsExactly( "One", "Two" );
		assertThat( dependencySpec.getOptionalDependencies() ).containsExactly( "Two", "Three", "Four" );
		assertThat( dependencySpec.getOrderInRole() ).isEqualTo( 1000 );
		assertThat( dependencySpec.getRole() ).isEqualTo( AcrossModuleRole.INFRASTRUCTURE );
	}
}
