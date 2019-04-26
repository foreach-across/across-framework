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
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.modules.webtest.config.WebTestWebResourcePackage;
import com.foreach.across.test.modules.webtest.controllers.WebResourceController;
import com.foreach.across.test.modules.webtest.controllers.WebResourcePackageController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.foreach.across.utils.CustomResultMatchers.jsoup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
@ContextConfiguration(classes = TestDeprecatedWebResourceRendering.Config.class)
public class TestDeprecatedWebResourceRendering
{
	@Autowired
	private AcrossContextInfo contextInfo;

	private MockMvc mockMvc;

	@Before
	public void initMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup( (WebApplicationContext) contextInfo.getApplicationContext() ).build();
	}

	@Test
	public void inlineJavascriptIsRendered() throws Exception {
		mockMvc.perform( get( WebResourceController.PATH ) )
		       .andExpect( status().isOk() )
		       // Assert css
		       .andExpect( jsoup().elementById( "inline-data-css" ).valueIgnoringLineEndings( "<style>test-css-inline</style><style>test-css-data</style>" ) )
		       .andExpect( jsoup().elementById( "not-inline-or-data-css" ).valueIgnoringLineEndings(
				       "<link rel=\"stylesheet\" href=\"test-css-external\"><link rel=\"stylesheet\" href=\"/across/resources/test-css-views\"><link rel=\"stylesheet\" href=\"test-css-relative\">" ) )

		       // Assert head javascript
		       .andExpect( jsoup().elementById( "inline-javascript" ).valueIgnoringLineEndings( "<script src=\"test-javascript-inline\"></script>" ) )
		       .andExpect( jsoup().elementById( "not-inline-and-data-javascript" ).valueIgnoringLineEndings(
				       "<script src=\"test-javascript-external\"></script>\n<script src=\"/across/resources/test-javascript-views\"></script>\n<script src=\"test-javascript-relative\"></script>" ) )
		       .andExpect( jsoup().elementById( "data-javascript" ).valueIgnoringLineEndings( "<script type=\"text/javascript\">\n" +
				                                                                                      "        (function ( Across ) {\n" +
				                                                                                      "            Across['' + \"test-javascript-data\"] = \"test-javascript-data-value\";\n" +
				                                                                                      "        })( window.Across = window.Across || {} );\n" +
				                                                                                      "    </script>" ) )

		       // Assert foot javascript
		       .andExpect( jsoup().elementById( "javascript-page-end" ).valueIgnoringLineEndings(
				       "<script src=\"test-javascript-end-external\"></script>\n<script src=\"/across/resources/test-javascript-end-views\"></script>\n<script src=\"test-javascript-end-relative\"></script>" ) )
		       .andExpect( jsoup().elementById( "javascript-page-end-data" ).valueIgnoringLineEndings(
				       "<script type=\"text/javascript\">        (function ( Across ) {            Across['' + \"test-javascript-end-data\"] = \"test-javascript-end-data-value\";        })( window.Across = window.Across || {} );    </script>" ) )
		       .andExpect(
				       jsoup().elementById( "javascript-page-end-inline" ).valueIgnoringLineEndings(
						       "<script src=\"test-javascript-end-inline\"></script>" ) );

	}

	@Test
	public void emptyBucket() throws Exception {
		mockMvc.perform( get( WebResourceController.PATH ) )
		       .andExpect( jsoup().elementById( "empty-bucket" ).valueIgnoringLineEndings( "" ) );
	}

	@Test
	public void cssBucket() throws Exception {
		mockMvc.perform( get( WebResourceController.PATH ) )
		       .andExpect( jsoup().elementById( "bucket-css" ).htmlMatches(
				       "<link rel='stylesheet' href='test-css-external' type='text/css'>" +
						       "<link rel='stylesheet' href='/across/resources/test-css-views' type='text/css'>" +
						       "<style type='text/css'>test-css-inline</style>" +
						       "<style type='text/css'>test-css-data</style>" +
						       "<link rel='stylesheet' href='/test-css-relative' type='text/css'>"
		       ) );
	}

	@Test
	public void javascriptBucket() throws Exception {
		mockMvc.perform( get( WebResourceController.PATH ) )
		       .andExpect( jsoup().elementById( "bucket-javascript" ).htmlMatches(
				       "<script type='text/javascript'>test-javascript-inline</script>" +
						       "<script type='text/javascript'>(function( _data ) { _data[ \"test-javascript-data\" ] = \"test-javascript-data-value\"; })( window[\"Across\"] = window[\"Across\"] || {} );</script>" +
						       "<script src='test-javascript-external' type='text/javascript'></script>" +
						       "<script src='/across/resources/test-javascript-views' type='text/javascript'></script>" +
						       "<script src='/test-javascript-relative' type='text/javascript'></script>"
		       ) );
	}

	@Test
	public void javascriptPageEndBucket() throws Exception {
		mockMvc.perform( get( WebResourceController.PATH ) )
		       .andExpect( jsoup().elementById( "bucket-javascript-page-end" ).htmlMatches(
				       "<script src='test-javascript-end-external' type='text/javascript'></script>" +
						       "<script src='/across/resources/test-javascript-end-views' type='text/javascript'></script>" +
						       "<script src='/test-javascript-end-relative' type='text/javascript'></script>" +
						       "<script type='text/javascript'>(function( _data ) { _data[ \"test-javascript-end-data\" ] = \"test-javascript-end-data-value\"; })( window[\"Across\"] = window[\"Across\"] || {} );</script>" +
						       "<script type='text/javascript'>test-javascript-end-inline</script>"
		       ) );
	}

	@Test
	public void inlineJavascriptIsRenderedForPackage() throws Exception {
		mockMvc.perform( get( WebResourcePackageController.PATH ) )
		       .andExpect( jsoup().elementById( "not-inline-or-data-css" ).valueIgnoringLineEndings(
				       "<link rel=\"stylesheet\" href=\"//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\">" ) )
		       .andExpect( jsoup().elementById( "javascript-page-end" )
		                          .valueIgnoringLineEndings( "<script src=\"//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js\"></script>" ) )
		       .andExpect( status().isOk() );
	}

	@Configuration
	@AcrossTestConfiguration(modules = { AcrossWebModule.NAME })
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossModule()
			{
				@Override
				public String getName() {
					return "WebResourceTestModule";
				}

				@Override
				public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
				                                 AcrossBootstrapConfig contextConfig ) {
					contextConfig.extendModule( "WebResourceTestModule", DummyModuleConfig.class );
				}
			} );
		}
	}

	@Configuration
	public static class DummyModuleConfig
	{
		@Bean
		public WebResourceController webResourceController() {
			return new WebResourceController();
		}

		@Bean
		public WebResourcePackageController webResourcePackageController() {
			return new WebResourcePackageController();
		}

		@Bean
		public WebTestWebResourcePackage webTestWebresourcePackage() {
			return new WebTestWebResourcePackage();
		}
	}
}
