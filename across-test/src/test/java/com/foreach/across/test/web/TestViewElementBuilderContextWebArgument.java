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

import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration(classes = TestDefaultTemplate.Config.class)
public class TestViewElementBuilderContextWebArgument
{
	@Autowired
	private MockMvc mvc;

	@Test
	public void modelAttributesShouldBeVisibleInTheBuilderContext() throws Exception {
		mvc.perform( get( "/viewElementBuilderContext" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString( "[model:modelA,modelB,modelC]" ) ) );
	}

	@Test
	public void customAttributesShouldOnlyBeVisibleInTheBuilderContext() throws Exception {
		mvc.perform( get( "/viewElementBuilderContext" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string(
				   containsString(
						   "[builderContext:builderContextA,builderContextB," +
								   "com.foreach.across.modules.web.context.WebAppLinkBuilder," +
								   "com.foreach.across.modules.web.resource.WebResourceRegistry," +
								   "com.foreach.across.modules.web.support.LocalizedTextResolver," +
								   "org.springframework.context.MessageSource]"
				   )
		   ) );
	}
}
