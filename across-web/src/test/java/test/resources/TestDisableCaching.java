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
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import test.AbstractWebIntegrationTest;
import test.modules.TestModules;
import test.modules.testResources.TestResourcesModule;

import static org.junit.Assert.assertFalse;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = TestDefaultResourceVersioning.Config.class)
@TestPropertySource(properties = {
		"acrossWebModule.resources.versioning.version=alpha",
		"acrossWebModule.resources.caching.enabled=false"
})
public class TestDisableCaching extends AbstractWebIntegrationTest
{
	@Test
	public void noCacheHeadersShouldBeSentButVersioningApplied() {
		HttpHeaders headers = headers( "/across/resources/css/alpha/testResources/parent.css" );
		assertFalse( headers.containsKey( HttpHeaders.CACHE_CONTROL ) );
		assertFalse( headers.containsKey( HttpHeaders.EXPIRES ) );

		headers = headers( "/across/resources/js/alpha/testResources/javascript.js" );
		assertFalse( headers.containsKey( HttpHeaders.CACHE_CONTROL ) );
		assertFalse( headers.containsKey( HttpHeaders.EXPIRES ) );
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
