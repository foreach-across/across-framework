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

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestMockMvcFromBuilder
{
	@Test
	public void characterEncodingFilterShouldApply() throws Exception {
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
}
