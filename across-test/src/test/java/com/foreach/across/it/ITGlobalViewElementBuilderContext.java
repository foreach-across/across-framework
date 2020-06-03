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
package com.foreach.across.it;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class ITGlobalViewElementBuilderContext
{
	@Test
	public void globalBuilderContextIsNotRegisteredExplicitly() throws Exception {
		try (AcrossTestWebContext ctx = web().register( TestController.class )
		                                     .modules( AcrossWebModule.NAME )
		                                     .property( "across.web.registerGlobalBuilderContext", "false" )
		                                     .build()) {
			assertFalse(
					ctx.moduleContainsLocalBean( AcrossWebModule.NAME, "viewElementBuilderContextInterceptor" )
			);

			ctx.mockMvc()
			   .perform( get( "/hello" ) )
			   .andExpect( status().isOk() )
			   .andDo( r -> assertFalse(
					   WebResourceUtils.getViewElementBuilderContext( r.getRequest() ).isPresent() ) );
		}
	}

	@Test
	public void byDefaultGlobalBuilderContextIsAvailable() throws Exception {
		try (AcrossTestWebContext ctx = web().register( TestController.class )
		                                     .modules( AcrossWebModule.NAME )
		                                     .build()) {
			ctx.getBeansOfTypeAsMap( WebMvcConfigurer.class );
			assertTrue(
					ctx.moduleContainsLocalBean( AcrossWebModule.NAME, "viewElementBuilderContextInterceptor" )
			);

			ctx.mockMvc()
			   .perform( get( "/hello" ) )
			   .andExpect( status().isOk() )
			   .andDo( r -> assertTrue( WebResourceUtils.getViewElementBuilderContext( r.getRequest() ).isPresent() ) );
		}
	}

	@Controller
	static class TestController
	{
		@ResponseBody
		@RequestMapping("/hello")
		String hello() {
			return "hello";
		}
	}
}
