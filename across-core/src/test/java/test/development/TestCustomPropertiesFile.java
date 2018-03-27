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
package test.development;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marc Vanbrabant
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestCustomPropertiesFile.Config.class)
public class TestCustomPropertiesFile
{
	private static final String MODULE_ONE = "one";
	private static final String MODULE_TWO = "two";

	private static Path file;
	private static Path temporaryDirectory;

	private AcrossDevelopmentMode developmentMode;

	@Autowired
	public void retrieveDevelopmentMode( AcrossContextBeanRegistry beanRegistry ) {
		developmentMode = beanRegistry.getBeanOfType( AcrossDevelopmentMode.class );
	}

	@BeforeClass
	public static void setup() throws IOException {
		file = Files.createTempFile( "across-development", ".properties" );
		temporaryDirectory = Files.createTempDirectory( "test-across-development" );
		Files.createDirectory( temporaryDirectory.resolve( MODULE_TWO ) );

		String propertyValue = temporaryDirectory.toString().replaceAll( "\\\\", "/" );
		Files.write(
				file,
				( "acrossModule.one.resources=" + propertyValue + "\n" +
						"acrossModule.two.resources=" + propertyValue ).getBytes()
		);
	}

	@AfterClass
	public static void cleanup() throws IOException {
		FileSystemUtils.deleteRecursively( temporaryDirectory.toFile() );
		Files.delete( file );
	}

	@Test
	public void developmentModeShouldBeActive() {
		assertTrue( developmentMode.isActive() );
	}

	@Test
	public void locationsAreLoadedAccordingToPrecedence() {
		Map<String, String> locations = developmentMode.getDevelopmentLocations( "." );
		// development properties file
		assertEquals( temporaryDirectory.resolve( "." ).toFile(), new File( locations.get( MODULE_ONE ) ) );
		// direct property
		assertEquals(
				temporaryDirectory.resolve( MODULE_TWO ).resolve( "." ).toFile(),
				new File( locations.get( MODULE_TWO ) )
		);
	}

	@Configuration
	@EnableAcrossContext
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.setProperty( "across.development.properties", "file:" + file.toString() );
			context.setProperty( "acrossModule.two.resources", temporaryDirectory.resolve( MODULE_TWO ).toString() );
			context.setDevelopmentMode( true );
			context.addModule( new EmptyAcrossModule( MODULE_ONE ) );
			context.addModule( new EmptyAcrossModule( MODULE_TWO ) );
		}
	}
}
