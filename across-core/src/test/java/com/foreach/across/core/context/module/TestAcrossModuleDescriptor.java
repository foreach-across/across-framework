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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossVersionInfo;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.installers.InstallerReference;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("AcrossModuleDescriptor creation")
@ExtendWith(MockitoExtension.class)
class TestAcrossModuleDescriptor
{
	private ApplicationContextConfigurer appConfigurer = new ComponentScanConfigurer( MyModule.class.getPackage().getName() + ".config" );
	private ApplicationContextConfigurer installerConfigurer = new ComponentScanConfigurer( MyModule.class.getPackage().getName() + ".installers.config" );
	private BeanFilter exposeFilter = BeanFilter.empty();
	private AcrossVersionInfo versionInfo = AcrossVersionInfo.UNKNOWN;

	@Mock
	private InstallerSettings installerSettings;

	@Mock
	private ExposedBeanDefinitionTransformer exposeTransformer;

	@Test
	@DisplayName("builder defaults")
	void builderDefaults() {
		AcrossModuleDescriptor descriptor = AcrossModuleDescriptor.builder()
		                                                          .moduleName( "defaultModule" )
		                                                          .resourcesKey( "defaultModule" )
		                                                          .build();

		assertThat( descriptor.getModuleName() ).isEqualTo( "defaultModule" );
		assertThat( descriptor.getResourcesKey() ).isEqualTo( "defaultModule" );
		assertThat( descriptor.getModuleNameAliases() ).isEmpty();
		assertThat( descriptor.isExtensionModule() ).isFalse();
		assertThat( descriptor.getExtensionTargets() ).isEmpty();
		assertThat( descriptor.getApplicationContextConfigurers() ).isEmpty();
		assertThat( descriptor.getInstallerContextConfigurers() ).isEmpty();
		assertThat( descriptor.getModuleConfigurationScanPackages() ).isEmpty();
		assertThat( descriptor.getExposeTransformer() ).isEmpty();
		assertThat( descriptor.getExposeFilter() ).isEmpty();
		assertThat( descriptor.getVersionInfo() ).isEqualTo( AcrossVersionInfo.UNKNOWN );
		assertThat( descriptor.getModuleRole() ).isEqualTo( AcrossModuleRole.APPLICATION );
		assertThat( descriptor.getOrderInModuleRole() ).isEqualTo( 0 );
		assertThat( descriptor.getInstallerSettings() ).isEmpty();
		assertThat( descriptor.getInstallerScanPackages() ).isEmpty();
		assertThat( descriptor.getProperties() ).isEmpty();
		assertThat( descriptor.getRequiredModules() ).isEmpty();
		assertThat( descriptor.getOptionalModules() ).isEmpty();
		assertThat( descriptor.isEnabled() ).isTrue();
		assertThat( descriptor.getInstallers() ).isEmpty();
	}

	@Test
	@DisplayName("using builder")
	void builder() {
		AcrossModuleDescriptor descriptor = AcrossModuleDescriptor
				.builder()
				.moduleName( "myModule" )
				.moduleNameAlias( "aliasOne" )
				.moduleNameAlias( "aliasTwo" )
				.resourcesKey( "myModuleResources" )
				.applicationContextConfigurer( appConfigurer )
				.installerContextConfigurer( installerConfigurer )
				.installerScanPackage( MyModule.class.getPackage().getName() + ".installers" )
				.moduleConfigurationScanPackage( MyModule.class.getPackage().getName() + ".extensions" )
				.installerSettings( installerSettings )
				.requiredModule( "One" )
				.optionalModule( "Two" )
				.optionalModule( "Three" )
				.moduleRole( AcrossModuleRole.INFRASTRUCTURE )
				.orderInModuleRole( 1000 )
				.exposeFilter( exposeFilter )
				.exposeTransformer( exposeTransformer )
				.versionInfo( versionInfo )
				.property( "one", 1 )
				.installer( InstallerReference.from( "SomeClassName" ) )
				.installer( InstallerReference.from( ExtensionModule.class ) )
				.installer( InstallerReference.from( 10 ) )
				.build();

		assertDescriptorProperties( descriptor );
		assertThat( descriptor.getModuleNameAliases() ).containsExactly( "aliasOne", "aliasTwo" );
	}

	@Test
	@DisplayName("to dependency spec")
	void asDependencySpec() {
		AcrossModuleDependencySorter.DependencySpec dependencySpec = AcrossModuleDescriptor
				.builder()
				.moduleName( "myModule" )
				.resourcesKey( "res" )
				.build()
				.toDependencySpec();

		assertThat( dependencySpec ).isNotNull();
		assertThat( dependencySpec.getNames() ).containsExactly( "myModule" );
		assertThat( dependencySpec.getRequiredDependencies() ).isEmpty();
		assertThat( dependencySpec.getOptionalDependencies() ).isEmpty();
		assertThat( dependencySpec.getOrderInRole() ).isEqualTo( 0 );
		assertThat( dependencySpec.getRole() ).isEqualTo( AcrossModuleRole.APPLICATION );

		dependencySpec = AcrossModuleDescriptor
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
				.build()
				.toDependencySpec();

		assertThat( dependencySpec ).isNotNull();
		assertThat( dependencySpec.getNames() ).containsExactly( "myModule", "aliasOne", "aliasTwo" );
		assertThat( dependencySpec.getRequiredDependencies() ).containsExactly( "One" );
		assertThat( dependencySpec.getOptionalDependencies() ).containsExactly( "Two", "Three" );
		assertThat( dependencySpec.getOrderInRole() ).isEqualTo( 1000 );
		assertThat( dependencySpec.getRole() ).isEqualTo( AcrossModuleRole.INFRASTRUCTURE );
	}

	@Test
	@DisplayName("from AcrossModule with @AcrossRole and @AcrossDepends")
	void acrossModuleWithAnnotations() {
		MyModule myModule = new MyModule();
		myModule.setInstallerSettings( installerSettings );
		myModule.setExposeTransformer( exposeTransformer );
		myModule.setProperty( "one", 1 );

		assertDescriptorProperties( AcrossModuleDescriptor.from( myModule ) );
	}

	@Test
	@DisplayName("from extension AcrossModule")
	void acrossModule() {
		ExtensionModule extensionModule = new ExtensionModule();
		extensionModule.setEnabled( false );

		AcrossModuleDescriptor descriptor = AcrossModuleDescriptor.from( extensionModule );

		assertThat( descriptor.getModuleName() ).isEqualTo( "extensionModule" );
		assertThat( descriptor.getResourcesKey() ).isEqualTo( "extensionModule" );
		assertThat( descriptor.getRequiredModules() ).isEmpty();
		assertThat( descriptor.getOptionalModules() ).isEmpty();
		assertThat( descriptor.getModuleRole() ).isEqualTo( AcrossModuleRole.APPLICATION );
		assertThat( descriptor.getOrderInModuleRole() ).isEqualTo( 200 );
		assertThat( descriptor.isExtensionModule() ).isTrue();
		assertThat( descriptor.getExtensionTargets() ).containsExactly( "MyModule", "OtherModule" );
		assertThat( descriptor.isEnabled() ).isFalse();
	}

	private void assertDescriptorProperties( AcrossModuleDescriptor descriptor ) {
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getModuleName() ).isEqualTo( "myModule" );
		assertThat( descriptor.getResourcesKey() ).isEqualTo( "myModuleResources" );
		assertThat( descriptor.getApplicationContextConfigurers() ).containsExactly( appConfigurer );
		assertThat( descriptor.getInstallerContextConfigurers() ).containsExactly( installerConfigurer );
		assertThat( descriptor.getInstallerScanPackages() ).containsExactly( MyModule.class.getPackage().getName() + ".installers" );
		assertThat( descriptor.getModuleConfigurationScanPackages() ).containsExactly( MyModule.class.getPackage().getName() + ".extensions" );
		assertThat( descriptor.getInstallerSettings() ).contains( installerSettings );
		assertThat( descriptor.getRequiredModules() ).containsExactly( "One" );
		assertThat( descriptor.getOptionalModules() ).containsExactly( "Two", "Three" );
		assertThat( descriptor.getModuleRole() ).isEqualTo( AcrossModuleRole.INFRASTRUCTURE );
		assertThat( descriptor.getOrderInModuleRole() ).isEqualTo( 1000 );
		assertThat( descriptor.getExposeFilter() ).contains( exposeFilter );
		assertThat( descriptor.getExposeTransformer() ).contains( exposeTransformer );
		assertThat( descriptor.getVersionInfo() ).isEqualTo( versionInfo );
		assertThat( descriptor.getProperties() ).containsEntry( "one", 1 ).containsOnlyKeys( "one" );
		assertThat( descriptor.isExtensionModule() ).isFalse();
		assertThat( descriptor.getExtensionTargets() ).isEmpty();
		assertThat( descriptor.isEnabled() ).isTrue();
		assertThat( descriptor.getInstallers() )
				.containsExactly( InstallerReference.from( "SomeClassName" ), InstallerReference.from( ExtensionModule.class ), InstallerReference.from( 10 ) );
	}

	@AcrossRole(value = AcrossModuleRole.INFRASTRUCTURE, order = 1000)
	@AcrossDepends(required = "One", optional = { "Two", "Three" })
	private class MyModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "myModule";
		}

		@Override
		public String getResourcesKey() {
			return "myModuleResources";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { "SomeClassName", ExtensionModule.class, 10 };
		}
	}

	private class ExtensionModule extends AcrossModule implements Ordered
	{
		@Override
		public String getName() {
			return "extensionModule";
		}

		@Override
		public String[] getExtensionTargets() {
			return new String[] { "MyModule", "OtherModule" };
		}

		@Override
		public int getOrder() {
			return 200;
		}
	}
}
