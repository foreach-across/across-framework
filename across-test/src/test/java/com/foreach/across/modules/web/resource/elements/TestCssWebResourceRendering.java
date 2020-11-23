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
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class TestCssWebResourceRendering extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleCssLink() {
		renderAndExpect( WebResource.css( "@webjars:/some-webjar" ),
		                 "<link rel='stylesheet' type='text/css' href='/webjars/some-webjar'/>" );
	}

	@Test
	public void linkWithRelAndTypeAndMedia() {
		renderAndExpect( WebResource.css( "@webjars:/some-webjar" ).rel( "stylesheet2" ).type( MediaType.APPLICATION_JSON ).media( "media-query" ),
		                 "<link href='/webjars/some-webjar' rel='stylesheet2' media='media-query' type='application/json' />" );
	}

	@Test
	public void linkWithCustomAttribute() {
		renderAndExpect( WebResource.css().url( "/test" ).attribute( "crossorigin", "anonymous" ),
		                 "<link type='text/css' href='/test' rel='stylesheet' crossorigin='anonymous' />" );
	}

	@Test
	public void simpleInline() {
		renderAndExpect( WebResource.css().inline( "body {background-color: powderblue;}" ),
		                 "<style type='text/css'>body {background-color: powderblue;}</style>" );
	}

	@Test
	public void inlineWithCustomAttributes() {
		renderAndExpect(
				WebResource.css().inline( "body {background-color: powderblue;}" )
				           .type( MediaType.APPLICATION_JSON )
				           .media( "media-query" )
				           .attribute( "data-something", 1 ),
				"<style type='application/json' media='media-query' data-something='1'>body {background-color: powderblue;}</style>" );
	}
}
