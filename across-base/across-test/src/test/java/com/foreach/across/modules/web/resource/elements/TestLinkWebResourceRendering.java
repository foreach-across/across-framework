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
import org.junit.Test;
import org.springframework.http.MediaType;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class TestLinkWebResourceRendering extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleLink() {
		renderAndExpect( WebResource.link( "@webjars:/some-webjar" ),
		                 "<link href='/webjars/some-webjar'/>" );
	}

	@Test
	public void withRelAndTypeAndMedia() {
		renderAndExpect( WebResource.link( "@webjars:/some-webjar" ).rel( "stylesheet" ).media( "media-query" ).type( MediaType.valueOf( "text/css" ) ),
		                 "<link href='/webjars/some-webjar' rel='stylesheet' media='media-query' type='text/css' />" );
	}

	@Test
	public void withCustomAttribute() {
		renderAndExpect( WebResource.link( "/test" ).attribute( "rel2", "stylesheet" ).crossOrigin( "anonymous" ),
		                 "<link href='/test' rel2='stylesheet' crossorigin='anonymous' />" );
	}
}
