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

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerMetaData;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import org.junit.Test;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestInstallerMetaData
{
	@Test(expected = IllegalArgumentException.class)
	public void noInstallerAnnotation() {
		InstallerMetaData.forClass( String.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void abstractClass() {
		InstallerMetaData.forClass( AbstractInstaller.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void interfaceClass() {
		InstallerMetaData.forClass( InstallerInterface.class );
	}

	@Test(expected = IllegalArgumentException.class)
	public void installerMetaDataIsNotInherited() {
		InstallerMetaData.forClass( ImplementedInstallerWithoutAnnotation.class );
	}

	@Test
	public void defaultSettings() {
		InstallerMetaData metaData = InstallerMetaData.forClass( DefaultSettingsInstaller.class );
		assertNotNull( metaData );
		assertEquals( DefaultSettingsInstaller.class, metaData.getInstallerClass() );
		assertEquals( DefaultSettingsInstaller.class.getName(), metaData.getName() );
		assertEquals( "does something", metaData.getDescription() );
		assertEquals( InstallerPhase.BeforeContextBootstrap, metaData.getInstallerPhase() );
		assertEquals( InstallerRunCondition.VersionDifferent, metaData.getRunCondition() );
		assertEquals( 1, metaData.getVersion() );
		assertNull( metaData.getGroup() );
		assertInstallerMethods( metaData, "publicInstallerMethod" );
	}

	@Test
	public void installerGroupAndCustomName() {
		InstallerMetaData metaData = InstallerMetaData.forClass( InstallerGroupInstaller.class );
		assertNotNull( metaData );
		assertEquals( InstallerGroupInstaller.class, metaData.getInstallerClass() );
		assertEquals( "CustomInstallerName", metaData.getName() );
		assertEquals( "with group", metaData.getDescription() );
		assertEquals( InstallerPhase.AfterModuleBootstrap, metaData.getInstallerPhase() );
		assertEquals( InstallerRunCondition.AlwaysRun, metaData.getRunCondition() );
		assertEquals( -2, metaData.getVersion() );
		assertEquals( "someGroup", metaData.getGroup() );
		assertInstallerMethods( metaData, "protectedInstallerMethod" );
	}

	@Test
	public void orderedInstallerMethods() {
		InstallerMetaData metaData = InstallerMetaData.forClass( ExtendedInstaller.class );
		assertNotNull( metaData );
		assertEquals( ExtendedInstaller.class, metaData.getInstallerClass() );
		assertEquals( ExtendedInstaller.class.getName(), metaData.getName() );
		assertEquals( "multi methods", metaData.getDescription() );
		assertEquals( InstallerPhase.BeforeContextBootstrap, metaData.getInstallerPhase() );
		assertEquals( InstallerRunCondition.VersionDifferent, metaData.getRunCondition() );
		assertEquals( 1, metaData.getVersion() );
		assertEquals( "inheritingGroup", metaData.getGroup() );
		assertInstallerMethods( metaData, "methodOne", "methodTwo", "abstractInstallerMethod" );
	}

	private void assertInstallerMethods( InstallerMetaData metaData, String... methodNames ) {
		Method[] methods = metaData.getInstallerMethods();
		assertNotNull( methods );
		assertEquals( methodNames.length, methods.length );
		for ( int i = 0; i < methodNames.length; i++ ) {
			assertEquals( methodNames[i], methods[i].getName() );
		}
	}

	@Installer(description = "does something")
	private static class DefaultSettingsInstaller
	{
		@InstallerMethod
		public void publicInstallerMethod() {
		}
	}

	@InstallerGroup("inheritingGroup")
	@Installer(description = "abstract installer with installer method")
	private static abstract class AbstractInstaller
	{
		@InstallerMethod
		public void abstractInstallerMethod() {
		}
	}

	@InstallerGroup("someGroup")
	@Installer(
			description = "with group",
			runCondition = InstallerRunCondition.AlwaysRun,
			version = -2,
			phase = InstallerPhase.AfterModuleBootstrap,
			name = "CustomInstallerName"
	)
	private static class InstallerGroupInstaller
	{
		@InstallerMethod
		protected void protectedInstallerMethod() {
			nonInstallerMethod();
		}

		public void nonInstallerMethod() {
		}
	}

	@Installer(description = "interface installer")
	private interface InstallerInterface
	{
	}

	private static class ImplementedInstallerWithoutAnnotation extends AbstractInstaller
	{
	}

	@Installer(description = "multi methods")
	private static class ExtendedInstaller extends AbstractInstaller
	{
		@InstallerMethod
		@Order(2)
		protected void methodTwo() {
		}

		@InstallerMethod
		@Order(1)
		public void methodOne() {
		}
	}
}
