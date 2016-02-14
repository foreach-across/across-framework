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
package com.foreach.across.test.modules.web.it.encoding;

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.test.modules.web.it.AbstractWebIntegrationTest;
import com.foreach.across.test.modules.web.it.modules.TestModules;
import com.foreach.across.test.modules.web.it.modules.testResources.TestResourcesModule;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = ITDefaultCharacterEncoding.Config.class)
public class ITDefaultCharacterEncoding extends AbstractWebIntegrationTest
{
	@Test
	public void defaultEncodingShouldBeUTF8() {
		assertEquals( "UTF-8", get( "/characterEncoding" ) );
	}

	@Test
	public void filterShouldBeRegistered() {
		assertEquals( "true", get( "filtered?name=characterEncodingFilter" ) );
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
