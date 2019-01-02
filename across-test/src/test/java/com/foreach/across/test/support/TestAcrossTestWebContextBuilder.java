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
package com.foreach.across.test.support;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.Test;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
						.modules( new EmptyAcrossModule( "Servlet configuration", ServletConfigurer.class ) )
						.build()
		) {
			assertTrue( ctx.getServletContext().isInitialized() );

			ServletConfigurer configurer = ctx.getBeanOfType( ServletConfigurer.class );
			assertNotNull( configurer );
			assertTrue( configurer.allowed );
		}
	}

	@Test
	public void dynamicRegistrationDisabled() {
		try (
				AcrossTestWebContext ctx = new AcrossTestWebContextBuilder()
						.dynamicServletContext( false )
						.modules( AcrossWebModule.NAME )
						.modules( new EmptyAcrossModule( "Servlet configuration", ServletConfigurer.class ) )
						.build()
		) {
			assertTrue( ctx.getServletContext().isInitialized() );

			ServletConfigurer configurer = ctx.getBeanOfType( ServletConfigurer.class );
			assertNotNull( configurer );
			assertFalse( configurer.allowed );
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

	@Test
	public void mockMvcCharacterEncodingFilterShouldApply() throws Exception {
		try (AcrossTestWebContext ctx = web().property( "build.number", "unit-test" )
		                                     .property( "spring.http.encoding.force", "true" )
		                                     .modules( AcrossWebModule.NAME )
		                                     .build()) {
			MockMvc mvc = ctx.mockMvc();

			mvc.perform( get( "/across/resources/static/unit-test/testResources/test.txt" ) )
			   .andExpect( status().isOk() )
			   .andExpect( content().string( is( "hùllµ€" ) ) );
		}
	}

	@Configuration
	public static class ServletConfigurer implements ServletContextInitializer
	{
		private boolean allowed;

		@Override
		public void onStartup( ServletContext servletContext ) {
			allowed = true;
		}
	}
}
