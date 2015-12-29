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
package com.foreach.across.test.modules.web.it.resourceurls;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.test.modules.web.it.AbstractWebIntegrationTest;
import com.foreach.across.test.modules.web.it.modules.TestModules;
import com.foreach.across.test.modules.web.it.modules.testResources.TestResourcesModule;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = ITDefaultResourceVersioning.Config.class)
@TestPropertySource(properties = "build.number=95247852")
public class ITDefaultResourceVersioning extends AbstractWebIntegrationTest
{
	@Test
	public void staticResourcesShouldBeServedUnderDefaultPath() {
		String output = get( "/across/resources/css/testResources/parent.css" );
		assertNotNull( output );
		assertTrue( output.contains( "body { color: black; }" ) );

		output = get( "/across/resources/js/testResources/javascript.js" );
		assertNotNull( output );
		assertTrue( output.contains( "alert('hello');" ) );
	}

	@Test
	public void thymeleafShouldReplaceResourceUrls() {
		String output = get( "/testResources" );
		assertNotNull( output );
		assertTrue( output.contains( "parent css: /across/resources/css/95247852/testResources/parent.css" ) );
		assertTrue( output.contains( "javascript: /across/resources/js/95247852/testResources/javascript.js" ) );
	}

	@Configuration
	@EnableAcrossContext(
			modules = TestResourcesModule.NAME,
			modulePackageClasses = { AcrossPlatform.class, TestModules.class }
	)
	public static class Config extends WebMvcConfigurerAdapter
	{
		// todo: doesnt need to be here, test needs to be adapted with the default resource versioning strategy in place
		/*@Override
		public void addResourceHandlers( ResourceHandlerRegistry registry ) {
			String resourceLocations = "classpath:/views/";
			AppCacheManifestTransformer appCacheTransformer = new AppCacheManifestTransformer();
			VersionResourceResolver versionResolver = new VersionResourceResolver()
					.addVersionStrategy( new FixedVersionStrategy( "build-1.1.0" ), "*//**" );
	 registry.addResourceHandler( "/across/resources*//**" )
	 .addResourceLocations( resourceLocations )
	 .setCachePeriod( 0 )
	 .resourceChain( false )
	 .addResolver( versionResolver )
	 .addTransformer( appCacheTransformer );
	 }*/
	}
}
