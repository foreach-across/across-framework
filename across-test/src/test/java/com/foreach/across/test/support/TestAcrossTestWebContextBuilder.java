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
package com.foreach.across.test.support;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestAcrossTestWebContextBuilder extends TestAcrossTestContextBuilder
{
	@Override
	protected AcrossTestWebContextBuilder contextBuilder() {
		return new AcrossTestWebContextBuilder();
	}

	@Test
	public void dynamicRegistrationAllowed() {
		try (
				AcrossTestWebContext ctx = new AcrossTestWebContextBuilder()
						.modules( AcrossWebModule.NAME )
						.register( ServletConfigurer.class )
						.build()
		) {
			assertTrue( ctx.getServletContext().isDynamicRegistrationAllowed() );

			ServletConfigurer configurer = ctx.getBeanOfType( ServletConfigurer.class );
			assertNotNull( configurer );
			assertEquals( Boolean.TRUE, configurer.getAllowed() );
		}
	}

	@Test
	public void dynamicRegistrationDisabled() {
		try (
				AcrossTestWebContext ctx = new AcrossTestWebContextBuilder()
						.dynamicServletContext( false )
						.modules( AcrossWebModule.NAME )
						.register( ServletConfigurer.class )
						.build()
		) {
			assertFalse( ctx.getServletContext().isDynamicRegistrationAllowed() );

			ServletConfigurer configurer = ctx.getBeanOfType( ServletConfigurer.class );
			assertNotNull( configurer );
			assertEquals( Boolean.FALSE, configurer.getAllowed() );
		}
	}

	@Test
	public void servletContextShouldHaveWebApplicationContextRegistered() {
		try (
				AcrossTestWebContext ctx = new AcrossTestWebContextBuilder()
						.dynamicServletContext( false )
						.modules( AcrossWebModule.NAME )
						.register( ServletConfigurer.class )
						.build()
		) {
			ApplicationContext applicationContext = ctx.contextInfo().getApplicationContext();

			assertNotNull( WebApplicationContextUtils.getWebApplicationContext( ctx.getServletContext() ) );
			assertSame( applicationContext,
			            WebApplicationContextUtils.getWebApplicationContext( ctx.getServletContext() ) );
		}
	}

	@Configuration
	public static class ServletConfigurer extends AcrossWebDynamicServletConfigurer
	{
		public Boolean allowed;

		public Boolean getAllowed() {
			return allowed;
		}

		@Override
		protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
			allowed = true;
		}

		@Override
		protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
			allowed = false;
		}
	}
}
