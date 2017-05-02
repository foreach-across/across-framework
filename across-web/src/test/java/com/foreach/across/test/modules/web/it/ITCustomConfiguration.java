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
package com.foreach.across.test.modules.web.it;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import com.foreach.across.test.modules.web.it.modules.TestModules;
import com.foreach.across.test.modules.web.it.modules.testResources.TestResourcesModule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = ITCustomConfiguration.Config.class)
@TestPropertySource(properties = { "acrossWebModule.resources.path=/static",
                                   "acrossWebModule.views.thymeleaf.enabled=false" })
public class ITCustomConfiguration extends AbstractWebIntegrationTest
{
	@Autowired
	private WebAppPathResolver pathResolver;

	@Test
	public void staticResourcesShouldBeServedUnderConfiguredPath() {
		String output = get( "/static/css/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"images/test.png\"); }" ) );

		output = get( "/static/js/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );
	}

	@Test(expected = HttpClientErrorException.class)
	public void thymeleafShouldBeDisabled() {
		get( "/testResources" );
	}

	@Test
	public void resourcePrefixesShouldBeRegistered() {
		assertEquals( "/static/pdf/list.pdf", pathResolver.path( "@resource:/pdf/list.pdf" ) );
		assertEquals( "/static/static/pdf/list.pdf", pathResolver.path( "@static:/pdf/list.pdf" ) );
	}

	@Configuration
	@EnableAcrossContext(
			modules = TestResourcesModule.NAME,
			modulePackageClasses = { AcrossPlatform.class, TestModules.class }
	)
	public static class Config extends WebMvcConfigurerAdapter
	{
	}
}
