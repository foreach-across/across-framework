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
package com.foreach.across.test.dynamic;

import com.foreach.across.core.AcrossVersionInfo;
import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.DynamicAcrossModule.DynamicApplicationModule;
import com.foreach.across.core.DynamicAcrossModule.DynamicInfrastructureModule;
import com.foreach.across.core.DynamicAcrossModule.DynamicPostProcessorModule;
import com.foreach.across.core.DynamicAcrossModuleFactory;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.test.dynamic.pkg.PkgMember;
import com.foreach.across.test.dynamic.pkg.config.PkgConfig;
import com.foreach.across.test.dynamic.pkg.extensions.PkgExtensionConfig;
import com.foreach.across.test.dynamic.pkg.installers.PkgInstaller;
import com.foreach.across.test.dynamic.pkg.installers.config.PkgInstallerConfig;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.Ordered;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestDynamicAcrossModuleFactory
{
	private DynamicAcrossModuleFactory factory;

	@Before
	public void createFactory() {
		factory = new DynamicAcrossModuleFactory();
	}

	@Test(expected = IllegalStateException.class)
	public void nameOrBasePackageIsRequired() throws Exception {
		factory.getObject();
	}

	@Test
	public void applicationModule() throws Exception {
		DynamicApplicationModule module = (DynamicApplicationModule) factory.setModuleName( "MyModule" ).getObject();

		assertNotNull( module );
		assertEquals( "MyModule", module.getName() );
		assertEquals( "MyModule", module.getResourcesKey() );
		assertEquals( Ordered.LOWEST_PRECEDENCE, module.getOrder() );

		AcrossVersionInfo versionInfo = module.getVersionInfo();
		assertNotNull( versionInfo );
		assertNotNull( versionInfo.getBuildTime() );
		assertEquals( "dynamic-module", versionInfo.getVersion() );
		assertEquals( "Across", versionInfo.getProjectName() );
	}

	@Test
	public void infrastructureModule() throws Exception {
		DynamicInfrastructureModule module = (DynamicInfrastructureModule) factory
				.setModuleRole( AcrossModuleRole.INFRASTRUCTURE )
				.setModuleName( "MyModule" )
				.getObject();

		assertNotNull( module );
		assertEquals( "MyModule", module.getName() );
		assertEquals( "MyModule", module.getResourcesKey() );
		assertEquals( Ordered.HIGHEST_PRECEDENCE, module.getOrder() );
	}

	@Test
	public void postProcessorModule() throws Exception {
		DynamicPostProcessorModule module = (DynamicPostProcessorModule) factory
				.setModuleRole( AcrossModuleRole.POSTPROCESSOR )
				.setModuleName( "MyModule" )
				.getObject();

		assertNotNull( module );
		assertEquals( "MyModule", module.getName() );
		assertEquals( "MyModule", module.getResourcesKey() );
		assertEquals( Ordered.LOWEST_PRECEDENCE, module.getOrder() );
	}

	@Test
	public void defaultSettings() throws Exception {
		DynamicAcrossModule module = factory
				.setModuleName( "SomeModule" )
				.setResourcesKey( "myResources" )
				.setOrder( 123 )
				.setBasePackage( PkgMember.class.getPackage().getName() )
				.getObject();

		assertNotNull( module );
		assertEquals( "SomeModule", module.getName() );
		assertEquals( "myResources", module.getResourcesKey() );
		assertEquals( 123, module.getOrder() );

		assertPackages( module );
	}

	@Test
	public void defaultFromBasePackage() throws Exception {
		DynamicAcrossModule module = factory
				.setBasePackageClass( PkgMember.class )
				.getObject();

		assertNotNull( module );
		assertEquals( "PkgModule", module.getName() );
		assertEquals( "pkg", module.getResourcesKey() );

		assertPackages( module );
	}

	@Test
	public void noFullComponentScan() throws Exception {
		DynamicAcrossModule module = factory
				.setFullComponentScan( false )
				.setBasePackage( PkgMember.class.getPackage().getName() )
				.getObject();

		assertNotNull( module );

		Set<ApplicationContextConfigurer> configurers = module.getApplicationContextConfigurers();
		assertEquals( 1, configurers.size() );
		assertArrayEquals(
				new String[] { PkgConfig.class.getPackage().getName() },
				configurers.iterator().next().componentScanPackages()
		);
	}

	private void assertPackages( DynamicAcrossModule module ) {
		Set<ApplicationContextConfigurer> configurers = module.getApplicationContextConfigurers();
		assertEquals( 1, configurers.size() );
		assertArrayEquals(
				new String[] { PkgMember.class.getPackage().getName() },
				configurers.iterator().next().componentScanPackages()
		);

		configurers = module.getInstallerContextConfigurers();
		assertEquals( 1, configurers.size() );
		assertArrayEquals(
				new String[] { PkgInstallerConfig.class.getPackage().getName() },
				configurers.iterator().next().componentScanPackages()
		);

		assertArrayEquals(
				new String[] { PkgInstaller.class.getPackage().getName() },
				module.getInstallerScanPackages()
		);

		assertArrayEquals(
				new String[] { PkgConfig.class.getPackage().getName(),
				               PkgExtensionConfig.class.getPackage().getName() },
				module.getModuleConfigurationScanPackages()
		);
	}
}
