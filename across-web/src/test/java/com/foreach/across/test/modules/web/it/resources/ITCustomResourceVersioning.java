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
package com.foreach.across.test.modules.web.it.resources;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.modules.web.it.AbstractWebIntegrationTest;
import com.foreach.across.test.modules.web.it.modules.TestModules;
import com.foreach.across.test.modules.web.it.modules.testResources.TestResourcesModule;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.FixedVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = ITCustomResourceVersioning.Config.class)
@TestPropertySource(properties = { "build.number=95247852", "acrossWebModule.resources.versioning.enabled=true" })
public class ITCustomResourceVersioning extends AbstractWebIntegrationTest
{
	@Test
	public void defaultCachingShouldBeEnabled() {
		HttpHeaders headers = headers( "/across/resources/css/1.0/testResources/parent.css" );
		assertEquals( "max-age=31536000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );

		headers = headers( "/across/resources/js/testResources/javascript.js" );
		assertEquals( "max-age=31536000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );
	}

	@Test
	public void staticResourcesShouldBeServedUnderVersionedPath() {
		String output = get( "/across/resources/css/1.0/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"images/test.png\"); }" ) );

		output = get( "/across/resources/js/custom-version/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );

		output = get( "/across/resources/static/1.0/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"./images/test.png\"); }" ) );

		output = get( "/across/resources/static/custom-version/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );
	}

	@Test
	public void thymeleafShouldReplaceResourceUrls() {
		String output = get( "/testResources" );
		assertNotNull( output );
		assertTrue( output.contains( "parent css: /across/resources/css/1.0/testResources/parent.css" ) );
		assertTrue( output.contains( "javascript: /across/resources/js/custom-version/testResources/javascript.js" ) );
		assertTrue( output.contains( "static css: /across/resources/static/1.0/testResources/parent.css" ) );
	}

	@Configuration
	@EnableAcrossContext(
			modules = TestResourcesModule.NAME,
			modulePackageClasses = { AcrossPlatform.class, TestModules.class }
	)
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public AcrossWebModule acrossWebModule() {
			AcrossWebModule webModule = new AcrossWebModule();
			webModule.addApplicationContextConfigurer( CustomVersionResourceResolver.class );

			return webModule;
		}
	}

	@Configuration
	public static class CustomVersionResourceResolver
	{
		@Bean
		public VersionResourceResolver versionResourceResolver() {
			return new VersionResourceResolver()
					.addVersionStrategy( new FixedVersionStrategy( "1.0" ), "/**/*.css" )
					.addVersionStrategy( new FixedVersionStrategy( "custom-version" ), "/**" );
		}
	}
}
