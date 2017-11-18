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
public class TestPrefixedControllerTemplates
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private MockMvc mvc;

	@Test
	public void configuredTemplateShouldApplyToValidControllerCall() throws Exception {
		mvc.perform( get( "/prefix/home" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString( "otherhead-child-otherfoot" ) ) )
		   .andExpect( content().string(
				   containsString( "resources:[/across/resources/other.css,/across/resources/controller.css]" ) ) );
	}

	@Test
	public void configuredTemplateShouldApplyToRegisteredExceptionHandler() throws Exception {
		mvc.perform( get( "/prefix/databaseError" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString( "otherhead-a database error has occurred-otherfoot" ) ) )
		   .andExpect( content().string( containsString( "resources:[/across/resources/other.css]" ) ) );
	}

	@Test
	public void clearTemplateOnRegisteredExceptionHandler() throws Exception {
		mvc.perform( get( "/prefix/illegalArgumentError" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( not( containsString( "resources" ) ) ) )
		   .andExpect( content().string( containsString( "a database error has occurred" ) ) );
	}

	@Test
	public void noTemplateWithPartialRendering() throws Exception {
		mvc.perform( get( "/prefix/home?_partial=content" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( not( containsString( "resources" ) ) ) )
		   .andExpect( content().string( containsString( "child" ) ) );
	}

	@Test
	public void noTemplateWithPartialOnRegisteredExceptionHandler() throws Exception {
		mvc.perform( get( "/prefix/databaseError?_partial=content" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( not( containsString( "resources" ) ) ) )
		   .andExpect( content().string( containsString( "a database error has occurred" ) ) );
	}

	@Test
	public void templateConfiguredOnExceptionHandlerInControllerAdvice() throws Exception {
		mvc.perform( get( "/prefix/runtimeError" ) )
		   .andExpect( status().is5xxServerError() )
		   .andExpect( content().string( containsString( "errorhead-Runtime error occurred.-errorfoot" ) ) )
		   .andExpect( content().string( containsString( "resources:[/across/resources/error.css]" ) ) );
	}

	@Test
	public void noTemplateOnExceptionHandlerInControllerAdviceIfPartial() throws Exception {
		mvc.perform( get( "/prefix/runtimeError?_partial=content" ) )
		   .andExpect( status().is5xxServerError() )
		   .andExpect( content().string( not( containsString( "resources" ) ) ) )
		   .andExpect( content().string( containsString( "Runtime error occurred." ) ) );
	}

	@AcrossTestConfiguration
	protected static class Config
	{
		@Bean
		public WebControllersModule webControllersModule() {
			return new WebControllersModule();
		}
	}
}

