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

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.MockAcrossServletContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.support.WebApplicationContextUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since Mar 2016
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration
@TestPropertySource(properties = {
		"build.number=unit-test",
})
public class TestNonAcrossMockMvcConfigurationWithAcrossWebProperty
{
	@Autowired
	private MockServletContext servletContext;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void servletContextShouldNotBeAcrossType() {
		assertFalse( servletContext instanceof MockAcrossServletContext );
	}

	@Test
	public void characterEncodingFilterShouldNotApply() throws Exception {
		mvc.perform( get( "/across/resources/static/unit-test/testResources/test.txt" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( is( not( "hùllµ€" ) ) ) );
	}

	@Test
	public void servletContextShouldHaveWebApplicationContextRegistered() {
		ApplicationContext applicationContext = contextInfo.getApplicationContext();

		assertNotNull( WebApplicationContextUtils.getWebApplicationContext( servletContext ) );
		assertSame( applicationContext,
		            WebApplicationContextUtils.getWebApplicationContext( servletContext ) );

	}

	@AcrossTestConfiguration
	protected static class Config
	{
		@Bean
		public AcrossWebModule acrossWebModule() {
			AcrossWebModule acrossWebModule = new AcrossWebModule();
			acrossWebModule.setProperty( "spring.http.encoding.force", true );
			return acrossWebModule;
		}
	}
}
