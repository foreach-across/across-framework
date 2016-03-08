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

package com.foreach.across.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestWebConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Bootstrap a an AcrossContext using the test configuration.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration
public class ITSimpleContextBootstrap
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void webModuleShouldHaveBeenBootstrapped() {
		assertTrue( SimpleInstaller.installed );
		assertTrue( contextInfo.getModuleInfo( AcrossWebModule.NAME ).isBootstrapped() );
	}

	@AcrossTestWebConfiguration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new MyModule() );
		}
	}

	static class MyModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "MyModule";
		}

		@Override
		public String getDescription() {
			return "Module that will force the installers to run.";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { SimpleInstaller.class };
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		}
	}

	@Installer(
			description = "Simple installer that will trigger the core installation.",
			runCondition = InstallerRunCondition.VersionDifferent,
			version = 1
	)
	static class SimpleInstaller
	{
		public static boolean installed = false;

		@InstallerMethod
		public void run() {
			installed = true;
		}
	}
}
