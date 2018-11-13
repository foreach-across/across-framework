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
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.across.test.modules.webtest.WebTestModule;
import com.foreach.across.test.modules.webtest.controllers.RenderViewElementController;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.XmlExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base integration test class for testing {@link com.foreach.across.modules.web.ui.ViewElement}
 * rendering using Thymeleaf.  This bootstraps an {@link com.foreach.across.core.AcrossContext}
 * with web support and the {@link com.foreach.across.test.modules.webtest.WebTestModule} enabled.
 * <p>
 * Will generate the output by running through the entire view resolving/Thymeleaf template stack and
 * allows xml assertion to be used on the generated result, thus ignoring whitespace and attribute ordering.
 * </p>
 * <p>See the {@link #renderAndExpect(com.foreach.across.modules.web.ui.ViewElement, java.lang.String)} method.</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration(value = "classpath:")
@ContextConfiguration(classes = AbstractViewElementTemplateTest.Config.class)
public abstract class AbstractViewElementTemplateTest
{
	/**
	 * Constants pointing to a custom template in across test library, along with
	 * the expected output if the template is being used.
	 */
	public static final String CUSTOM_TEMPLATE = "th/WebTestModule/customTemplate";
	public static final String CUSTOM_TEMPLATE_OUTPUT = "Hello from RenderViewElementController.";

	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired
	private RenderViewElementController renderController;

	private MockMvc mockMvc;
	private RenderViewElementController.Callback callback;
	private String template;

	@Before
	public void initMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup( (WebApplicationContext) contextInfo.getApplicationContext() ).build();
	}

	/**
	 * <p>Set the callback method to be called before rendering the view element.
	 * If set this way, the callback will be used for every test method.  See
	 * {@link #renderAndExpect(ViewElement, RenderViewElementController.Callback, String)} to execute a
	 * callback a single time only.
	 * </p>
	 * <p>Set to {@code null} to remove the callback.</p>
	 *
	 * @param callback to be executed before rendering
	 */
	protected void setCallback( RenderViewElementController.Callback callback ) {
		this.callback = callback;
	}

	/**
	 * Set a custom Thymeleaf template that should be used for rendering.
	 *
	 * @param template to use
	 */
	protected void setTemplate( String template ) {
		this.template = template;
	}

	/**
	 * Renders a single {@link com.foreach.across.modules.web.ui.ViewElement} by dispatching it to the
	 * render controller.  Verifies the generated content by using xml comparison.
	 *
	 * @param viewElement     element to render
	 * @param expectedContent xml body that should be generated
	 */
	public void renderAndExpect( ViewElement viewElement, final String expectedContent ) {
		renderAndExpect( viewElement, callback, expectedContent );
	}

	/**
	 * Renders a single {@link com.foreach.across.modules.web.ui.ViewElement} by dispatching it to the
	 * render controller.  Verifies the generated content by using xml comparison.
	 *
	 * @param viewElement     element to render
	 * @param callback        to be executed before rendering
	 * @param expectedContent xml body that should be generated
	 */
	public void renderAndExpect( ViewElement viewElement,
	                             RenderViewElementController.Callback callback,
	                             final String expectedContent ) {
		renderController.setTemplate( template );
		renderController.setElement( viewElement );
		renderController.setCallback( callback );

		final String pattern = "<?xml version=\"1.0\"?>" + generateDocType() + "<root xmlns:across='http://across.foreach.be'>%s</root>";

		try {
			mockMvc.perform( get( RenderViewElementController.PATH ) )
			       .andExpect( status().isOk() )
			       .andDo( mvcResult -> {
				       String receivedContent = mvcResult.getResponse().getContentAsString();

				       try {
					       new XmlExpectationsHelper().assertXmlEqual(
							       String.format( pattern, expectedContent ),
							       String.format( pattern, receivedContent )
					       );
				       }
				       catch ( AssertionError | Exception e ) {
					       throw new AssertionError( "Unexpected content:\n" + receivedContent, e );
				       }
			       } );

		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * @return output that was rendered
	 */
	public String render( ViewElement viewElement ) {
		return render( viewElement, callback );
	}

	/**
	 * @return output that was rendered
	 */
	public String render( ViewElement viewElement, RenderViewElementController.Callback callback ) {
		renderController.setElement( viewElement );
		renderController.setCallback( callback );
		renderController.setTemplate( template );

		try {
			return mockMvc.perform( get( RenderViewElementController.PATH ) )
			              .andExpect( status().isOk() )
			              .andReturn()
			              .getResponse().getContentAsString();
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private String generateDocType() {
		Collection<String> allowedEntities = allowedXmlEntities();
		if ( !allowedEntities.isEmpty() ) {
			StringBuilder doctype = new StringBuilder( "<!DOCTYPE doc_type [" );
			allowedEntities.forEach( s -> doctype.append( "<!ENTITY " ).append( s ).append( " \"&#160;\">" ) );
			doctype.append( "]>" );
			return doctype.toString();
		}
		return "";
	}

	/**
	 * Override this method to define the named entities that can be referenced in the xml.
	 */
	protected Collection<String> allowedXmlEntities() {
		return Collections.emptyList();
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
