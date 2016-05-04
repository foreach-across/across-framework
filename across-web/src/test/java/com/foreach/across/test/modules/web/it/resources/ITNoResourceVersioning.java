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
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.modules.web.it.AbstractWebIntegrationTest;
import com.foreach.across.test.modules.web.it.modules.TestModules;
import com.foreach.across.test.modules.web.it.modules.testResources.TestResourcesModule;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = ITDefaultResourceVersioning.Config.class)
@TestPropertySource(properties = {
		"acrossWebModule.resources.versioning.enabled=false",
		"acrossWebModule.resources.folders=js,css,custom",
		"acrossWebModule.resources.caching.period=1000",
		"build.number=95247852"
})
public class ITNoResourceVersioning extends AbstractWebIntegrationTest
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void customCachingShouldBeEnabled() {
		HttpHeaders headers = headers( "/across/resources/css/testResources/parent.css" );
		assertEquals( "max-age=1000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );

		headers = headers( "/across/resources/js/testResources/javascript.js" );
		assertEquals( "max-age=1000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );

		headers = headers( "/across/resources/custom/test.txt" );
		assertEquals( "max-age=1000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );
	}

	@Test
	public void staticResourcesShouldBeServedUnderDefaultPath() {
		String output = get( "/across/resources/css/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"images/test.png\"); }" ) );

		output = get( "/across/resources/js/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );
	}

	@Test
	public void thymeleafShouldReplaceResourceUrls() {
		String output = get( "/testResources" );
		assertNotNull( output );
		assertTrue( output.contains( "parent css: /across/resources/css/testResources/parent.css" ) );
		assertTrue( output.contains( "javascript: /across/resources/js/testResources/javascript.js" ) );
	}

	@Test
	public void staticShouldNotBeServed() {
		assertTrue( notFound( "/across/resources/static/testResources/javascript.js" ) );
		assertTrue( notFound( "/across/resources/static/testResources/parent.css" ) );
	}

	@Test
	public void customShouldBeServed() {
		String output = get( "/across/resources/custom/test.txt" );
		assertEquals( "some text", output );
	}

	@Test
	public void versioningRelatedBeansShouldNotExist() {
		assertNoWebBean( VersionResourceResolver.class );
		assertNoWebBean( AppCacheManifestTransformer.class );
		assertNoWebBean( ResourceUrlProvider.class );
		assertNoWebBean( ResourceUrlProviderExposingInterceptor.class );
	}

	private void assertNoWebBean( Class<?> beanType ) {
		boolean found = true;

		try {
			beanRegistry.getBeanOfTypeFromModule( AcrossWebModule.NAME, beanType );
		}
		catch ( NoSuchBeanDefinitionException nsbe ) {
			found = false;
		}

		assertFalse( "Bean of type " + beanType.getName() + " was found", found );
	}

	@Configuration
	@EnableAcrossContext(
			modules = TestResourcesModule.NAME,
			modulePackageClasses = { AcrossPlatform.class, TestModules.class }
	)
	public static class Config
	{

	}
}
