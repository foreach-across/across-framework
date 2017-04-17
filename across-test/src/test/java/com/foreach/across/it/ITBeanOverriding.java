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
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static com.foreach.across.test.support.AcrossTestBuilders.standard;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DevelopmentModeCondition that configurers added after the initial configurers will override the bean definition.
 *
 * @author Arne Vandamme
 */
public class ITBeanOverriding
{
	@Test
	public void nonOverriddenBeanDefinition() {
		try (AcrossTestContext ctx = standard().configurer( new Config() ).build()) {
			Object beanOne = ctx.getBeanFromModule( "MyModule", "beanOne" );

			assertNotNull( beanOne );
			assertEquals( "beanOne.MyModuleConfig", beanOne );
		}
	}

	@Test
	public void overriddenBeanDefinition() {
		try (AcrossTestContext ctx = standard().configurer( new OverridingConfig(), new Config() ).build()) {
			Object beanOne = ctx.getBeanFromModule( "MyModule", "beanOne" );

			assertNotNull( beanOne );
			assertEquals( 123, beanOne );
		}
	}

	@Test
	public void nonOverriddenBeanDefinitionWebContext() {
		try (AcrossTestContext ctx = web().configurer( new Config() ).build()) {
			Object beanOne = ctx.getBeanFromModule( "MyModule", "beanOne" );

			assertNotNull( beanOne );
			assertEquals( "beanOne.MyModuleConfig", beanOne );
		}
	}

	@Test
	public void overriddenBeanDefinitionWebContext() {
		try (AcrossTestContext ctx = web().configurer( new OverridingConfig(), new Config() ).build()) {
			Object beanOne = ctx.getBeanFromModule( "MyModule", "beanOne" );

			assertNotNull( beanOne );
			assertEquals( 123, beanOne );
		}
	}

	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.setInstallerAction( InstallerAction.DISABLED );
			context.addModule( new MyModule() );
		}
	}

	static class OverridingConfig implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossModule()
			{
				@Override
				public String getName() {
					return "OverridingModule";
				}

				@Override
				public String getDescription() {
					return null;
				}

				@Override
				protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
				}

				@Override
				public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
				                                 AcrossBootstrapConfig contextConfig ) {
					contextConfig.extendModule( "MyModule", ReplacedConfig.class );
				}
			} );
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
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new AnnotatedClassConfigurer( MyModuleConfig.class ) );
		}
	}

	@Configuration
	static class MyModuleConfig
	{
		@Bean
		public String beanOne() {
			return "beanOne.MyModuleConfig";
		}
	}

	@Configuration
	static class ReplacedConfig
	{
		@Bean
		public Integer beanOne() {
			return 123;
		}
	}
}
