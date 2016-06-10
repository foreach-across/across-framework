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
package com.foreach.across.test.development;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Marc Vanbrabant
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestCustomPropertiesFile.Config.class)
public class TestCustomPropertiesFile
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	private static Path file;
	private static Path temporaryDirectory;

	@BeforeClass
	public static void setup() throws IOException {
		file = Files.createTempFile( "across-development", ".properties" );
		temporaryDirectory = Files.createTempDirectory( "test-across-development" );
		Files.createDirectory( Paths.get( temporaryDirectory.toString(), TestModule.NAME ) );
		Files.write( file, ( "acrossModule.TestModule.resources=" + temporaryDirectory.toString().replaceAll( "\\\\",
		                                                                                                      "/" ) )
				.getBytes() );
	}

	@AfterClass
	public static void cleanup() throws IOException {
		FileSystemUtils.deleteRecursively( temporaryDirectory.toFile() );
		Files.delete( file );
	}

	@Test
	public void developmentLocationsAreLoaded() {
		AcrossDevelopmentMode acrossDevelopmentMode = beanRegistry.getBeanOfType( AcrossDevelopmentMode.class );
		assertTrue( acrossDevelopmentMode.isActive() );
		assertEquals( 1, acrossDevelopmentMode.getDevelopmentLocations( "/" ).size() );
		assertEquals( 1, acrossDevelopmentMode.getDevelopmentLocationsForResourcePath( "." ).size() );
	}

	@Configuration
	@EnableAcrossContext(TestModule.NAME)
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.setProperty( "across.development.properties", file.toString() );
			assertFalse( context.isDevelopmentMode() );
			context.setDevelopmentMode( true );
		}
	}

	public static final class TestModule extends AcrossModule
	{

		public static final String NAME = "TestModule";

		@Override
		public String getName() {
			return NAME;
		}
	}
}
