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
package com.foreach.across.test.application;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.ExposeForTest;
import com.foreach.across.test.application.app.DummyApplication;
import com.foreach.across.test.application.app.application.controllers.NonExposedComponent;
import com.foreach.across.test.support.config.MockMvcConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bootstrap using a {@link com.foreach.across.test.MockAcrossServletContext}.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(classes = { DummyApplication.class, MockMvcConfiguration.class })
@AcrossWebAppConfiguration
@ExposeForTest(NonExposedComponent.class)
public class TestSpringBootMockMvc
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired(required = false)
	private NonExposedComponent nonExposedComponent;

	@Test
	public void modulesShouldBeRegistered() {
		assertTrue( contextInfo.hasModule( "emptyModule" ) );
		assertTrue( contextInfo.hasModule( AcrossWebModule.NAME ) );
		assertTrue( contextInfo.hasModule( "DummyApplicationModule" ) );
		assertTrue( contextInfo.hasModule( "DummyInfrastructureModule" ) );

		assertFalse( contextInfo.hasModule( "DummyPostProcessorModule" ) );
	}

	@Test
	public void controllersShouldSayHello() throws Exception {
		assertContent( "application says hello", get( "/application" ) );
		assertContent( "infrastructure says hello", get( "/infrastructure" ) );
	}

	@Test
	public void versionedResourceShouldBeReturned() throws Exception {
		assertContent( "hùllµ€", get( "/res/static/boot-1.0/testResources/test.txt" ) );
	}

	@Test
	public void manuallyExposedComponent() {
		assertNotNull( nonExposedComponent );
	}

	private void assertContent( String expected, RequestBuilder requestBuilder ) throws Exception {
		mockMvc.perform( requestBuilder )
		       .andExpect( status().isOk() )
		       .andExpect( content().string( is( expected ) ) );
	}
}
