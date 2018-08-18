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
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestContext;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static com.foreach.across.test.support.AcrossTestBuilders.standard;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class ITAcrossTestContextBootstrap
{
	@Before
	public void resetInstaller() {
		SimpleInstaller.installed = false;
	}

	@Test
	public void bootstrapWithoutWebModuleAndCamelCaseProperties() {
		try (AcrossTestContext ctx = standard().configurer( new Config() )
		                                       .property( "customProps.myProperty", "camelCaseIsSupported" )
		                                       .property( "customProps.conditionalOnProperty.enabled", "true" )
		                                       .build()) {
			assertTrue( SimpleInstaller.installed );
			assertTrue( ctx.contextInfo().getModuleInfo( "MyModule" ).isBootstrapped() );
			assertFalse( ctx.contextInfo().hasModule( AcrossWebModule.NAME ) );

			assertSingleAcrossContextCreated( ctx );

			CustomProps props = ctx.getBeanOfType( CustomProps.class );
			assertEquals( "camelCaseIsSupported", props.getMyProperty() );

			assertTrue( ctx.containsBean( "conditionalOnPropertyEnabled" ) );
			assertFalse( ctx.containsBean( "conditionalOnPropertyDisabled" ) );
			assertEquals( "conditionalOnPropertyEnabled", ctx.getBean( "conditionalOnPropertyEnabled" ) );
		}
	}

	@Test
	public void bootstrapWithWebModuleAndKebabCaseProperties() {
		try (AcrossTestContext ctx = web().modules( AcrossWebModule.NAME )
		                                  .configurer( new Config() )
		                                  .property( "custom-props.my-property", "kebabCaseIsSupported" )
		                                  .property( "custom-props.conditional-on-property.enabled", "true" )
		                                  .build()) {
			assertTrue( SimpleInstaller.installed );
			assertTrue( ctx.contextInfo().getModuleInfo( "MyModule" ).isBootstrapped() );
			assertTrue( ctx.contextInfo().hasModule( AcrossWebModule.NAME ) );

			assertSingleAcrossContextCreated( ctx );

			CustomProps props = ctx.getBeanOfType( CustomProps.class );
			assertEquals( "kebabCaseIsSupported", props.getMyProperty() );

			assertTrue( ctx.containsBean( "conditionalOnPropertyEnabled" ) );
			assertFalse( ctx.containsBean( "conditionalOnPropertyDisabled" ) );
			assertEquals( "conditionalOnPropertyEnabled", ctx.getBean( "conditionalOnPropertyEnabled" ) );
		}
	}

	private void assertSingleAcrossContextCreated( AcrossTestContext ctx ) {
		String actualContextId = ctx.getContextId();
		String nextContextId = new AcrossContext( null ).getId();

		int actualId = Integer.valueOf( StringUtils.substringAfterLast( actualContextId, "-" ) );
		int nextId = Integer.valueOf( StringUtils.substringAfterLast( nextContextId, "-" ) );
		assertEquals( "An additional unexpected AcrossContext has been created", actualId + 1, nextId );
	}

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
			contextConfigurers.add( new AnnotatedClassConfigurer( CustomProps.class, ConditionalBeanConfiguration.class ) );
		}
	}

	@Exposed
	@ConfigurationProperties("custom-props")
	@Data
	private static class CustomProps
	{
		private String myProperty;
	}

	@Configuration
	@Exposed
	public static class ConditionalBeanConfiguration
	{
		@ConditionalOnProperty(value = "custom-props.conditional-on-property.enabled", havingValue = "true")
		@Bean
		public String conditionalOnPropertyEnabled() {
			return "conditionalOnPropertyEnabled";
		}

		@ConditionalOnProperty(value = "custom-props.conditional-on-property.enabled", havingValue = "false")
		@Bean
		public String conditionalOnPropertyDisabled() {
			return "conditionalOnPropertyDisabled";
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
