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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration(classes = TestDefaultTemplate.Config.class)
@TestPropertySource(properties = "build.number=fixed")
public class TestStaticAttribute
{
	@Autowired
	private MockMvc mvc;

	@Test
	public void defaultAttributeNames() throws Exception {

		verify( "auto static a", "a", "href" );
		verify( "auto static link", "link", "href" );
		verify( "auto static script", "script", "src" );
		verify( "auto static image", "image", "xlink:href" );
		verify( "auto static use", "use", "xlink:href" );

		verify( "auto resource a", "a", "href" );
		verify( "auto resource link", "link", "href" );
		verify( "auto resource script", "script", "src" );
		verify( "auto resource image", "image", "xlink:href" );
		verify( "auto resource use", "use", "xlink:href" );
	}

	@Test
	public void manualAttributeNames() throws Exception {
		verify( "manual static xlink", "a", "xlink:href" );
		verify( "manual static data-image-url", "span", "data-image-url" );
		verify( "manual resource xlink", "a", "xlink:href" );
		verify( "manual resource data-image-url", "span", "data-image-url" );
	}

	@Test
	public void dynamicAttributeValues() throws Exception {
		verify( "dynamic static href", "a", "href" );
	}

	@Test
	public void manualUrls() throws Exception {
		verify( "manual resource url", "a", "href" );
		verify( "manual static url", "a", "other" );
	}

	private void verify( String linkName, String element, String attribute ) throws Exception {
		mvc.perform( get( "/ctxPath/attributes" ).contextPath( "/ctxPath" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( containsString(
				   "<" + element + " " + attribute + "=\"/ctxPath/across/resources/static/fixed/testResources/test.txt\">" + linkName + "</" + element + ">" ) ) );
	}
}
