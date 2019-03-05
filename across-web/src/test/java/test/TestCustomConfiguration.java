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
package test;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import test.modules.TestModules;
import test.modules.testResources.TestResourcesModule;

import static com.foreach.across.modules.web.AcrossWebModuleSettings.WEBJARS_RESOURCES_PATH;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = TestCustomConfiguration.Config.class)
@TestPropertySource(properties = { "acrossWebModule.resources.path=/static",
                                   WEBJARS_RESOURCES_PATH + "=//cdn.jsdelivr.net/webjars/org.webjars/",
                                   "acrossWebModule.views.thymeleaf.enabled=false" })
public class TestCustomConfiguration extends AbstractWebIntegrationTest
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
		assertEquals( "//cdn.jsdelivr.net/webjars/org.webjars/bootstrap/3.1.0/css/bootstrap.min.css", pathResolver.path( "@webjars:/bootstrap/3.1.0/css/bootstrap.min.css" ) );
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
