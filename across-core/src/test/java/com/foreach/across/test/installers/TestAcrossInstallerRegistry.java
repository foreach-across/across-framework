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

package com.foreach.across.test.installers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AnnotationConfigBootstrapApplicationContextFactory;
import com.foreach.across.core.context.bootstrap.BootstrapApplicationContextFactory;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ProvidedBeansConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.*;
import com.foreach.across.test.modules.installer.installers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestAcrossInstallerRegistry
{
	private final InstallerMetaData BEFORE_CTX_META
			= InstallerMetaData.forClass( AlwaysRunBeforeContextBootstrapInstaller.class );
	private final InstallerMetaData AFTER_MOD_META
			= InstallerMetaData.forClass( AlwaysRunAfterModuleBootstrapInstaller.class );
	private final InstallerMetaData VERSION_META
			= InstallerMetaData.forClass( VersionBasedInstaller.class );

	private AcrossBootstrapInstallerRegistry registry;
	private AcrossBootstrapConfig contextConfig;
	private ModuleBootstrapConfig moduleConfig;
	private AcrossInstallerRepository installerRepository;
	private AcrossModule module;

	private InstallerSettings contextSettings;

	@Before
	public void setup() {
		installerRepository = mock( AcrossInstallerRepository.class );

		BootstrapApplicationContextFactory applicationContextFactory
				= new AnnotationConfigBootstrapApplicationContextFactory();

		AcrossContextBeanRegistry beanRegistry = mock( AcrossContextBeanRegistry.class );
		when( beanRegistry.getBeanOfType( AcrossInstallerRepository.class ) ).thenReturn( installerRepository );

		AcrossApplicationContext applicationContext = new AcrossApplicationContext();
		applicationContext.refresh();
		applicationContext.start();
		applicationContext.getBeanFactory().registerSingleton( "someBean", "beforeContextBootstrap" );
		applicationContext.getBeanFactory().registerSingleton( "acrossContextBeanRegistry", beanRegistry );

		AcrossApplicationContextHolder acrossApplicationContextHolder = mock( AcrossApplicationContextHolder.class );
		when( acrossApplicationContextHolder.getApplicationContext() ).thenReturn( applicationContext );
		when( acrossApplicationContextHolder.getBeanFactory() ).thenReturn(
				(AcrossListableBeanFactory) applicationContext.getBeanFactory() );

		AcrossContext acrossContext = mock( AcrossContext.class );
		when( acrossContext.hasApplicationContext() ).thenReturn( true );
		when( acrossContext.getAcrossApplicationContextHolder() ).thenReturn( acrossApplicationContextHolder );

		module = mock( AcrossModule.class );
		when( module.getName() ).thenReturn( "module" );
		contextConfig = mock( AcrossBootstrapConfig.class );

		AcrossContextInfo contextInfo = mock( AcrossContextInfo.class );
		when( contextInfo.getBootstrapConfiguration() ).thenReturn( contextConfig );

		applicationContext.getBeanFactory().registerSingleton( "acrossContextInfo", contextInfo );

		moduleConfig = mock( ModuleBootstrapConfig.class );
		when( moduleConfig.getModule() ).thenReturn( module );
		when( moduleConfig.getModuleName() ).thenReturn( "module" );

		contextSettings = mock( InstallerSettings.class );

		when( contextConfig.getContext() ).thenReturn( acrossContext );
		when( contextConfig.getModule( anyString() ) ).thenReturn( moduleConfig );
		when( contextConfig.getInstallerSettings() ).thenReturn( contextSettings );

		registry = new AcrossBootstrapInstallerRegistry( contextConfig, null, applicationContextFactory );
	}

	@After
	public void clean() {
		TestInstaller.reset();
	}

	@Test
	public void moduleSettingsUsedIfContextSettingsNotDisabled() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );

		InstallerSettings moduleSettings = mock( InstallerSettings.class );
		when( moduleConfig.getInstallerSettings() ).thenReturn( moduleSettings );

		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );
		when( moduleSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.DISABLED );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( contextSettings ).shouldRun( eq( "module" ), eq( BEFORE_CTX_META ) );
		verify( moduleSettings ).shouldRun( eq( "module" ), eq( BEFORE_CTX_META ) );

		// Nothing executed
		assertExecuted();
	}

	@Test
	public void moduleSettingsNotUsedIfContextSettingsDisabled() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );

		InstallerSettings moduleSettings = mock( InstallerSettings.class );
		when( moduleConfig.getInstallerSettings() ).thenReturn( moduleSettings );

		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.DISABLED );
		when( moduleSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.FORCE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( contextSettings ).shouldRun( eq( "module" ), eq( BEFORE_CTX_META ) );
		verify( moduleSettings, never() ).shouldRun( eq( "module" ), eq( BEFORE_CTX_META ) );

		// Nothing executed
		assertExecuted();
	}

	@Test
	public void installerBeansAreNotWiredIfNotExecuted() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );

		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.SKIP );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertTrue( TestInstaller.WIRED_VALUES.isEmpty() );

		assertExecuted();
	}

	@Test
	public void installerBeansAreWiredInParentIfNoModuleContextAvailable() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );

		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertExecuted( AlwaysRunBeforeContextBootstrapInstaller.class );
		assertEquals(
				"beforeContextBootstrap",
				TestInstaller.WIRED_VALUES.get( AlwaysRunBeforeContextBootstrapInstaller.class )
		);
	}

	@Test
	public void installerContextConfigurationTrumpsParentContext() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );

		Set<ApplicationContextConfigurer> configurers = Collections.singleton(
				new ProvidedBeansConfigurer( Collections.singletonMap( "someBean", "fromInstallerContext" ) )
		);
		when( moduleConfig.getInstallerContextConfigurers() ).thenReturn( configurers );

		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertExecuted( AlwaysRunBeforeContextBootstrapInstaller.class );
		assertEquals(
				"fromInstallerContext",
				TestInstaller.WIRED_VALUES.get( AlwaysRunBeforeContextBootstrapInstaller.class )
		);
	}

	@Test
	public void installerBeansAreWiredInModuleContextIfAvailable() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		AcrossApplicationContext applicationContext = new AcrossApplicationContext();
		applicationContext.refresh();
		applicationContext.start();
		applicationContext.getBeanFactory().registerSingleton( "someBean", "fromModuleContext" );

		AcrossApplicationContextHolder moduleAcrossApplicationContextHolder = mock(
				AcrossApplicationContextHolder.class );
		when( moduleAcrossApplicationContextHolder.getApplicationContext() ).thenReturn( applicationContext );
		when( moduleAcrossApplicationContextHolder.getBeanFactory() )
				.thenReturn( (AcrossListableBeanFactory) applicationContext.getBeanFactory() );

		when( module.hasApplicationContext() ).thenReturn( true );
		when( module.getAcrossApplicationContextHolder() ).thenReturn( moduleAcrossApplicationContextHolder );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertExecuted( AlwaysRunBeforeContextBootstrapInstaller.class );
		assertEquals(
				"fromModuleContext",
				TestInstaller.WIRED_VALUES.get( AlwaysRunBeforeContextBootstrapInstaller.class )
		);
	}

	@Test
	public void installerContextConfigurationTrumpsModuleContext() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		Set<ApplicationContextConfigurer> configurers = Collections.singleton(
				new ProvidedBeansConfigurer( Collections.singletonMap( "someBean", "fromInstallerContext" ) )
		);
		when( moduleConfig.getInstallerContextConfigurers() ).thenReturn( configurers );

		AcrossApplicationContext applicationContext = new AcrossApplicationContext();
		applicationContext.refresh();
		applicationContext.start();
		applicationContext.getBeanFactory().registerSingleton( "someBean", "fromModuleContext" );

		AcrossApplicationContextHolder moduleAcrossApplicationContextHolder = mock(
				AcrossApplicationContextHolder.class );
		when( moduleAcrossApplicationContextHolder.getApplicationContext() ).thenReturn( applicationContext );
		when( moduleAcrossApplicationContextHolder.getBeanFactory() )
				.thenReturn( (AcrossListableBeanFactory) applicationContext.getBeanFactory() );

		when( module.hasApplicationContext() ).thenReturn( true );
		when( module.getAcrossApplicationContextHolder() ).thenReturn( moduleAcrossApplicationContextHolder );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertExecuted( AlwaysRunBeforeContextBootstrapInstaller.class );
		assertEquals(
				"fromInstallerContext",
				TestInstaller.WIRED_VALUES.get( AlwaysRunBeforeContextBootstrapInstaller.class )
		);
	}

	@Test
	public void alwaysRunInstallerShouldExecute() {
		installers(
				AlwaysRunBeforeContextBootstrapInstaller.class,
				AlwaysRunAfterModuleBootstrapInstaller.class
		);
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository, never() ).getInstalledVersion( module.getName(), BEFORE_CTX_META.getName() );
		verify( installerRepository ).setInstalled( module.getName(), BEFORE_CTX_META );
		verify( installerRepository, never() ).setInstalled( module.getName(), AFTER_MOD_META );

		assertExecuted(
				AlwaysRunBeforeContextBootstrapInstaller.class
		);
	}

	@Test
	public void registerActionShouldLogButNotExecuteInstallerMethod() {
		installers(
				AlwaysRunAfterModuleBootstrapInstaller.class
		);
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.REGISTER );

		registry.runInstallersForModule( "module", InstallerPhase.AfterModuleBootstrap );

		verify( installerRepository ).setInstalled( module.getName(), AFTER_MOD_META );

		assertExecuted();
	}

	@Test
	public void versionBasedShouldNotExecuteIfInstalledVersionEqual() {
		installers(
				VersionBasedInstaller.class
		);
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		when( installerRepository.getInstalledVersion( module.getName(), VERSION_META.getName() ) )
				.thenReturn( VersionBasedInstaller.VERSION );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository, never() ).setInstalled( module.getName(), VERSION_META );

		assertExecuted();
	}

	@Test
	public void versionBasedShouldNotExecuteIfInstalledVersionHigher() {
		installers( VersionBasedInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		when( installerRepository.getInstalledVersion( module.getName(), VERSION_META.getName() ) )
				.thenReturn( VersionBasedInstaller.VERSION + 1 );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository, never() ).setInstalled( module.getName(), VERSION_META );
		assertExecuted();
	}

	@Test
	public void versionBasedShouldExecuteIfInstalledVersionLower() {
		installers( VersionBasedInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		when( installerRepository.getInstalledVersion( module.getName(), VERSION_META.getName() ) )
				.thenReturn( VersionBasedInstaller.VERSION - 1 );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository ).setInstalled( module.getName(), VERSION_META );

		assertExecuted( VersionBasedInstaller.class );
	}

	@Test
	public void forceActionShouldExecuteEvenIfNoVersionMatch() {
		installers( VersionBasedInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.FORCE );

		when( installerRepository.getInstalledVersion( module.getName(), VERSION_META.getName() ) )
				.thenReturn( VersionBasedInstaller.VERSION );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository ).setInstalled( module.getName(), VERSION_META );

		assertExecuted( VersionBasedInstaller.class );
	}

	@Test
	public void skipActionShouldNotExecuteInstaller() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.SKIP );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository, never() ).setInstalled( module.getName(), BEFORE_CTX_META );

		assertExecuted();
	}

	@Test
	public void disableActionShouldNotExecuteInstaller() {
		installers( AlwaysRunBeforeContextBootstrapInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.DISABLED );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository, never() ).setInstalled( module.getName(), BEFORE_CTX_META );

		assertExecuted();
	}

	@Test
	public void moduleNameAndMetaDataShouldBePassedToSettings() {
		installers( VersionBasedInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.DISABLED );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( contextSettings ).shouldRun( "module", VERSION_META );
	}

	@Test
	public void multipleInstallerMethodsShouldExecute() {
		installers( MultipleMethodInstaller.class );

		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		// The order does not matter for this test
		Set<Class<?>> expected = new HashSet<>( Arrays.asList( MultipleMethodInstaller.class, String.class,
		                                                       Object.class ) );
		Set<Class<?>> actual = new HashSet<>( TestInstaller.EXECUTED );

		assertEquals( expected, actual );
	}

	@Test
	public void installerShouldNotExecuteIfDependencyNotMet() {
		InstallerMetaData meta = InstallerMetaData.forClass( AlwaysRunWithDependencyInstaller.class );

		installers( AlwaysRunWithDependencyInstaller.class );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		verify( installerRepository, never() ).getInstalledVersion( module.getName(), meta.getName() );
		verify( installerRepository, never() ).setInstalled( module.getName(), meta );

		assertExecuted();
	}

	@Test
	public void installerShouldExecuteIfDependencyIsMet() {
		installers( AlwaysRunWithDependencyInstaller.class );

		when( contextConfig.hasModule( "requiredModule" ) ).thenReturn( true );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertExecuted( AlwaysRunWithDependencyInstaller.class );
	}

	@Test
	public void multipleInstallersAreExecutedInOrder() {
		installers(
				AlwaysRunWithDependencyInstaller.class,
				AlwaysRunBeforeContextBootstrapInstaller.class
		);

		when( contextConfig.hasModule( "requiredModule" ) ).thenReturn( true );
		when( contextSettings.shouldRun( anyString(), anyObject() ) ).thenReturn( InstallerAction.EXECUTE );

		registry.runInstallersForModule( "module", InstallerPhase.BeforeContextBootstrap );

		assertExecuted(
				AlwaysRunWithDependencyInstaller.class,
				AlwaysRunBeforeContextBootstrapInstaller.class
		);
	}

	@SuppressWarnings("unchecked")
	private void installers( Class... installerClass ) {
		Collection collection = Arrays.asList( installerClass );
		when( moduleConfig.getInstallers() ).thenReturn( collection );
	}

	private void assertExecuted( Class... installerClasses ) {
		assertArrayEquals( installerClasses, TestInstaller.executed() );
	}
}
