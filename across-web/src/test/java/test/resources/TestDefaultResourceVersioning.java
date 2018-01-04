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
package test.resources;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.web.AcrossWebModule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import test.AbstractWebIntegrationTest;
import test.modules.TestModules;
import test.modules.testResources.TestResourcesModule;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = TestDefaultResourceVersioning.Config.class)
@TestPropertySource(properties = "build.number=95247852")
public class TestDefaultResourceVersioning extends AbstractWebIntegrationTest
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void cachingShouldBeEnabled() {
		HttpHeaders headers = headers( "/across/resources/css/testResources/parent.css" );
		assertEquals( "max-age=31536000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );

		headers = headers( "/across/resources/js/testResources/javascript.js" );
		assertEquals( "max-age=31536000", headers.get( HttpHeaders.CACHE_CONTROL ).get( 0 ) );
	}

	@Test
	public void staticResourcesShouldBeServedUnderDefaultPath() {
		String output = get( "/across/resources/css/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"images/test.png\"); }" ) );

		output = get( "/across/resources/js/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );

		output = get( "/across/resources/static/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"./images/test.png\"); }" ) );

		output = get( "/across/resources/static/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );
	}

	@Test
	public void staticResourcesShouldBeServedUnderVersionedPath() {
		String output = get( "/across/resources/css/95247852/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"images/test.png\"); }" ) );

		output = get( "/across/resources/js/95247852/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );

		output = get( "/across/resources/static/95247852/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { background: url(\"./images/test.png\"); }" ) );

		output = get( "/across/resources/static/95247852/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );
	}

	@Test
	public void thymeleafShouldReplaceResourceUrls() {
		String output = get( "/testResources" );
		assertNotNull( output );
		assertTrue( output.contains( "parent css: /across/resources/css/95247852/testResources/parent.css" ) );
		assertTrue( output.contains( "javascript: /across/resources/js/95247852/testResources/javascript.js" ) );
		assertTrue( output.contains( "static css: /across/resources/static/95247852/testResources/parent.css" ) );
	}

	@Test
	public void customShouldNotBeServed() {
		assertTrue( notFound( "/across/resources/custom/test.txt" ) );
	}

	@Test
	public void versioningRelatedBeansShouldExist() {
		assertNotNull( beanRegistry.getBeanOfTypeFromModule( AcrossWebModule.NAME, VersionResourceResolver.class ) );
		assertNotNull( beanRegistry.getBeanOfTypeFromModule(
				AcrossWebModule.NAME, AppCacheManifestTransformer.class
		) );
		assertNotNull( beanRegistry.getBeanOfTypeFromModule( AcrossWebModule.NAME, ResourceUrlProvider.class ) );
		assertNotNull( beanRegistry.getBeanOfTypeFromModule(
				AcrossWebModule.NAME, ResourceUrlProviderExposingInterceptor.class
		) );
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
