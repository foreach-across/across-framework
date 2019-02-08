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
package com.foreach.across.modules.web.resource;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.ExposeForTest;
import com.foreach.across.test.application.app.DummyApplication;
import com.foreach.across.test.application.app.application.controllers.NonExposedComponent;
import com.foreach.across.test.modules.webtest.WebTestModule;
import com.foreach.across.test.modules.webtest.controllers.WebResourceController;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import com.foreach.across.test.support.config.MockMvcConfiguration;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.foreach.across.utils.CustomResultMatchers.jsoup;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration(value = "classpath:")
@ContextConfiguration(classes = ITWebResource.Config.class)
public class ITWebResource
{
	@Autowired
	private AcrossContextInfo contextInfo;

	private MockMvc mockMvc;

	@Before
	public void initMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup( (WebApplicationContext) contextInfo.getApplicationContext() ).build();
	}

	@Test
	public void testInlineJavascriptRendered() throws Exception {
		mockMvc.perform( get( WebResourceController.PATH ) )
		       .andExpect( jsoup().elementById("inline-data-javascript").value("<script type=\"text/javascript\">\n" +
				                                                                       "        (function ( Across ) {\n" +
				                                                                       "            Across['' + \"test-javascript-data\"] = \"test-javascript-data\";\n" +
				                                                                       "        })( window.Across = window.Across || {} );\n" +
				                                                                       "    </script>"))
		       .andExpect( jsoup().elementById("not-inline-and-data-javascript").value("<script src=\"test-javascript-external\"></script>\n<script src=\"/across/resourcestest-javascript-views\"></script>\n<script src=\"test-javascript-relative\"></script>"))
		       .andExpect( jsoup().elementById("javascript-page-end").value("<script src=\"test-javascript-end-external\"></script>\n<script src=\"/across/resourcestest-javascript-end-views\"></script>\n<script src=\"test-javascript-end-data\"></script>\n<script src=\"test-javascript-end-relative\"></script>"))
		       .andExpect( jsoup().elementById("javascript-page-end").value("<script src=\"test-javascript-end-external\"></script>\n<script src=\"/across/resourcestest-javascript-end-views\"></script>\n<script src=\"test-javascript-end-data\"></script>\n<script src=\"test-javascript-end-relative\"></script>"))
		       .andExpect( jsoup().elementById("javascript-bottom-scripts").value(""))
		       .andExpect( jsoup().elementById("javascript-page-end").value("<script src=\"test-javascript-end-external\"></script>\n" +
				                                                                    "<script src=\"/across/resourcestest-javascript-end-views\"></script>\n" +
				                                                                    "<script src=\"test-javascript-end-data\"></script>\n" +
				                                                                    "<script src=\"test-javascript-end-relative\"></script>"))
		       .andExpect( status().isOk() )
		;

	}

	// testDataJavascript
	// testExternalJavascript
	// testInternalCss
	// testDataCss
	// testExternalCss
	// testInternalWebjar

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new WebTestModule() );
			context.addModule( new AcrossWebModule() );
		}
	}
}
