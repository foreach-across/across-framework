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
package com.foreach.across.modules.web.resource.elements;

import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.TextViewElementBuilder;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class TestJavascriptWebResourceRendering extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleJavascriptLink() {
		renderAndExpect( WebResource.javascript( "@webjars:/bootstrap.js" ),
		                 "<script type='text/javascript' src='/webjars/bootstrap.js'/>" );
	}

	@Test
	public void linkWithTypeAndAsyncAndDefer() {
		renderAndExpect( WebResource.javascript( "@webjars:/some-webjar" ).async().defer().type( MediaType.TEXT_HTML ),
		                 "<script src='/webjars/some-webjar' async='async' defer='defer' type='text/html' />" );
	}

	@Test
	public void linkWithCustomAttribute() {
		renderAndExpect( WebResource.javascript().url( "/test" ).attribute( "crossorigin", "anonymous" ),
		                 "<script type='text/javascript' src='/test' crossorigin='anonymous' />" );
	}

	@Test
	public void linkResetsInline() {
		renderAndExpect( WebResource.javascript().inline( "hello" ).url( "/test" ),
		                 "<script type='text/javascript' src='/test' />" );
	}

	@Test
	public void simpleInline() {
		renderAndExpect( WebResource.javascript().inline( "hello" ),
		                 "<script type='text/javascript'>hello</script>" );
	}

	@Test
	public void inlineResetsLink() {
		renderAndExpect( WebResource.javascript().url( "/test" ).inline( "hello" ),
		                 "<script type='text/javascript'>hello</script>" );
	}

	@Test
	public void inlineIgnoresAsyncDefer() {
		renderAndExpect( WebResource.javascript().inline( "hello" ).async().defer().type( MediaType.TEXT_HTML ),
		                 "<script type='text/html'>hello</script>" );
	}

	@Test
	public void inlineWithCustomAttributes() {
		renderAndExpect(
				WebResource.javascript()
				           .inline( new TextViewElementBuilder().text( "<hello>" ) )
				           .type( MediaType.APPLICATION_JSON )
				           .crossOrigin( "anonymous" )
				           .attribute( "data-something", 1 ),
				"<script type='application/json' crossorigin='anonymous' data-something='1'>&lt;hello&gt;</script>" );
	}

	@Test
	public void globalJsonDataOutput() {
		Map<String, String> statics = new HashMap<String, String>()
		{{
			put( "static", "/" );
			put( "admin", "/admin" );
		}};

		renderAndExpect(
				JavascriptWebResourceBuilder.globalJsonData( "Across.AcrossWebModule", statics ),
				"(function( _data ) { _data[ \"AcrossWebModule\" ] = {\"static\":\"/\",\"admin\":\"/admin\"}; })( window[\"Across\"] = window[\"Across\"] || {} );"
		);
	}

	@Test
	public void multipleGlobalJsonData() {
		renderAndExpect(
				WebResource.javascript()
				           .inline(
						           new ContainerViewElementBuilder()
								           .add( JavascriptWebResourceBuilder.globalJsonData( "MyApp.one", 1 ) )
								           .add( JavascriptWebResourceBuilder.globalJsonData( "MyApp.two", 2 ) )
				           ),
				"<script type='text/javascript'>" +
						"(function( _data ) { _data[ \"one\" ] = 1; })( window[\"MyApp\"] = window[\"MyApp\"] || {} );" +
						"(function( _data ) { _data[ \"two\" ] = 2; })( window[\"MyApp\"] = window[\"MyApp\"] || {} );" +
						"</script>"
		);
	}

	@Test
	public void globalJsonDataScript() {
		renderAndExpect(
				WebResource.globalJsonData( "MyApp.Settings.Enabled", true ),
				"<script type='text/javascript'>" +
						"(function( _data ) { _data[ \"Settings.Enabled\" ] = true; })( window[\"MyApp\"] = window[\"MyApp\"] || {} );" +
						"</script>"
		);
	}
}
