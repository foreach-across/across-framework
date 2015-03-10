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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.across.test.modules.webtest.WebTestModule;
import com.foreach.across.test.modules.webtest.controllers.RenderViewElementController;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.XmlExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base integration test class for testing {@link com.foreach.across.modules.web.ui.ViewElement}
 * rendering using Thymeleaf.  This bootstraps an {@link com.foreach.across.core.AcrossContext}
 * with web support and the {@link com.foreach.across.test.modules.webtest.WebTestModule} enabled.
 * <p/>
 * Will generate the output by running through the entire view resolving/Thymeleaf template stack and
 * allows xml assertion to be used on the generated result, thus ignoring whitespace and attribute ordering.
 * <p/>
 * See the {@link #renderAndExpect(com.foreach.across.modules.web.ui.ViewElement, java.lang.String)} method.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = AbstractViewElementTemplateTest.Config.class)
public abstract class AbstractViewElementTemplateTest
{
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private RenderViewElementController renderController;

	private MockMvc mockMvc;

	@Before
	public void initMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup( wac ).build();
	}

	/**
	 * Renders a single {@link com.foreach.across.modules.web.ui.ViewElement} by dispatching it to the
	 * render controller.  Verifies the generated content by using xml comparison.
	 *
	 * @param viewElement     element to render
	 * @param expectedContent xml body that should be generated
	 */
	public void renderAndExpect( ViewElement viewElement, final String expectedContent ) {
		renderController.setElement( viewElement );

		final String expectedXml =
				"<?xml version=\"1.0\"?><root xmlns:across='http://across.foreach.be'>" + expectedContent + "</root>";

		try {
			mockMvc.perform( get( RenderViewElementController.PATH ) )
			       .andExpect( status().isOk() )
			       .andDo( new ResultHandler()
			       {
				       @Override
				       public void handle( MvcResult mvcResult ) throws Exception {
					       String receivedXml = mvcResult.getResponse().getContentAsString();
					       try {
						       new XmlExpectationsHelper().assertXmlEqual( expectedXml, receivedXml );
					       }
					       catch ( AssertionError ae ) {
						       throw new AssertionError( "Unexpected content:\n" + receivedXml, ae );
					       }
				       }
			       } );

		}
		catch ( AssertionError ae ) {
			throw ae;
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new WebTestModule() );
		}
	}
}