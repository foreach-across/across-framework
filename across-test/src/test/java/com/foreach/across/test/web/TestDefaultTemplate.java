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
package com.foreach.across.test.web;

import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.web.module.WebControllersModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
public class TestDefaultTemplate
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private MockMvc mvc;

	// AX-123
	@Test
	public void defaultTemplateShouldApplyToValidControllerCall() throws Exception {
		mvc.perform( get( "/home" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString( "header-child-footer" ) ) )
		   .andExpect( content().string( containsString( "resources:[/across/resources/test.css,/across/resources/controller.css]" ) ) );
	}

	// AX-123 - registry should be reset and handled
	@Test
	public void defaultTemplateShouldApplyToRegisteredExceptionHandler() throws Exception {
		mvc.perform( get( "/databaseError" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString( "header-a database error has occurred-footer" ) ) )
		   .andExpect( content().string( containsString( "resources:[/across/resources/test.css]" ) ) );
	}

	@Test
	public void clearTemplateOnRegisteredExceptionHandler() throws Exception {
		mvc.perform( get( "/illegalArgumentError" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( not( containsString( "resources" ) ) ) )
		   .andExpect( content().string( containsString( "a database error has occurred" ) ) );
	}

	@Test
	public void defaultTemplateWithPartialRendering() throws Exception {
		mvc.perform( get( "/home?_partial=content" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( not( containsString( "header" ) ) ) )
		   .andExpect( content().string( containsString( "child" ) ) );
	}

	@Test
	public void defaultTemplateWithPartialOnRegisteredExceptionHandler() throws Exception {
		mvc.perform( get( "/databaseError?_partial=content" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( not( containsString( "header" ) ) ) )
		   .andExpect( content().string( containsString( "a database error has occurred" ) ) );
	}

	@Test
	public void defaultTemplateWithDefaultExceptionHandlerShouldHaveCorrectNestedException() throws Exception {
		thrown.expectMessage( containsString( "Runtime error occurred." ) );

		mvc.perform( get( "/runtimeError" ) );
	}

	@Test
	public void defaultTemplateWithPartialOnDefaultExceptionHandlerShouldHaveCorrectNestedException() throws Exception {
		thrown.expectMessage( containsString( "Runtime error occurred." ) );

		mvc.perform( get( "/runtimeError?_partial=content" ) );
	}

	@AcrossTestConfiguration
	static class Config
	{
		@Bean
		public WebControllersModule webControllersModule() {
			return new WebControllersModule();
		}
	}
}

