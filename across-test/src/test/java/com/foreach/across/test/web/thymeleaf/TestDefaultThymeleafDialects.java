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
package com.foreach.across.test.web.thymeleaf;

import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.web.TestDefaultTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@ExtendWith(SpringExtension.class)
@AcrossWebAppConfiguration(classes = TestDefaultTemplate.Config.class)
public class TestDefaultThymeleafDialects
{
	@Autowired
	private MockMvc mvc;

	@Test
	public void temporalsAttributeShouldBeAvailable() throws Exception {
		mvc.perform( get( "/thymeleafDialects" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString( "java8time:21-Nov-2017" ) ) );
	}
}

