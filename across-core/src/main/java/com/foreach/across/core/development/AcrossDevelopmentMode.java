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
package com.foreach.across.core.development;

import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.context.AcrossModuleEntity;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Bean that is created if development mode is active on the AcrossContext.
 *
 * @author Arne Vandamme
 */
public class AcrossDevelopmentMode
{
	private static final String MODULE_PROPERTY_PREFIX = "acrossModule.";
	private static final String RESOURCE_PROPERTY_SUFFIX = ".resources";

	private static final Logger LOG = LoggerFactory.getLogger( AcrossDevelopmentMode.class );

	public static final String PROPERTIES = "across.development.properties";

	private final String buildId = "dev:" + UUID.randomUUID().toString();

	@Autowired
	private Environment environment;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired
	private ApplicationContext applicationContext;

	private final Map<String, String> moduleResourcePaths = new HashMap<>();

	@PostConstruct
	private void loadProperties() {
		if ( isActive() ) {
			String propertiesResourceLocation = environment.resolvePlaceholders(
					environment.getProperty( PROPERTIES, "file:${user.home}/dev-configs/across-devel.properties" )
			);

			Resource developmentProperties = applicationContext.getResource( propertiesResourceLocation );

			registerDynamicModuleResourcesRelativeToWorkingDirectory();

			if ( developmentProperties.exists() ) {
				LOG.info( "Loading development properties from {}", developmentProperties );

				Properties props = new Properties();

				try (InputStream is = developmentProperties.getInputStream()) {
					props.load( is );

					registerModuleProperties( props );
				}
				catch ( IOException ioe ) {
					LOG.warn( "Failed to load development properties from {}", developmentProperties, ioe );
				}
			}

			registerEnvironmentModuleResources();
		}
	}

	private void registerDynamicModuleResourcesRelativeToWorkingDirectory() {
		File basePath = Paths.get( "src/main/resources" ).toFile();
		if ( basePath.exists() ) {
			contextInfo
					.getModules()
					.forEach( module -> {
						if ( module.getModule() instanceof DynamicAcrossModule ) {
							moduleResourcePaths.put( module.getName(), basePath.getAbsolutePath() );
						}
					} );
		}
	}

	private void registerEnvironmentModuleResources() {
		for ( AcrossModuleInfo module : contextInfo.getModules() ) {
			String propertyName = MODULE_PROPERTY_PREFIX + module.getName() + RESOURCE_PROPERTY_SUFFIX;
			String location = environment.getProperty( propertyName );

			if ( location != null ) {
				moduleResourcePaths.put( module.getName(), location );
			}
		}
	}

	private void registerModuleProperties( Properties properties ) {
		for ( Map.Entry<Object, Object> entry : properties.entrySet() ) {
			String propertyName = (String) entry.getKey();

			if ( propertyName.startsWith( MODULE_PROPERTY_PREFIX ) ) {
				propertyName = propertyName.replace( MODULE_PROPERTY_PREFIX, "" );

				if ( propertyName.endsWith( RESOURCE_PROPERTY_SUFFIX ) ) {
					String module = propertyName.replace( RESOURCE_PROPERTY_SUFFIX, "" );

					moduleResourcePaths.put( module, (String) entry.getValue() );
				}
			}
		}
	}

	/**
	 * @return True if development mode is enabled on the context.
	 */
	public boolean isActive() {
		return contextInfo.getContext().isDevelopmentMode();
	}

	/**
	 * Get a unique build id that changes between application restarts.
	 *
	 * @return unique build id
	 */
	public String getBuildId() {
		return buildId;
	}

	/**
	 * Determines a physical location for any path in the resources folder of a module.
	 *
	 * @param module Module instance.
	 * @param path   Relative path to resolve.
	 * @return Physical path or null if it could not be resolved.
	 */
	public String getDevelopmentLocation( AcrossModuleEntity module, String path ) {
		return getDevelopmentLocations( path ).get( module.getResourcesKey() );
	}

	/**
	 * Determines the map of physical resource locations for module resources.
	 * This method is different in that it simply resolves a physical directory
	 * instead of an assumed module resource directory.  Stated differently: the
	 * resource key is not included in the generated locations.
	 *
	 * @return Map containing the resource key and physical location.
	 */
	public Map<String, String> getDevelopmentLocations( String path ) {
		Map<String, String> locations = new HashMap<>();

		if ( isActive() ) {
			for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
				String resourceKey = moduleInfo.getResourcesKey();

				String location = moduleResourcePaths.get( moduleInfo.getName() );

				if ( location != null ) {
					Path dir = Paths.get( location ).resolve( path );

					if ( dir.toFile().exists() ) {
						locations.put( resourceKey, dir.toString() );
					}
				}
			}
		}

		return locations;
	}

	/**
	 * Determines a physical location for a particular module resource.
	 *
	 * @param module Module instance.
	 * @param path   Relative path to resolve.
	 * @return Physical path or null if it could not be resolved.
	 */
	public String getDevelopmentLocationForResourcePath( AcrossModuleEntity module, String path ) {
		return getDevelopmentLocationsForResourcePath( path ).get( module.getResourcesKey() );
	}

	/**
	 * Determines the map of physical resource locations to use for a given type of resource
	 * (identified by the path).  The key in the returned map is the module path for the resources.
	 * This method will iterate over all registered modules and see if they have a local development
	 * location available.
	 *
	 * @param path Path identifying the type of resource.
	 * @return Map containing the resource key and physical location.
	 */
	public Map<String, String> getDevelopmentLocationsForResourcePath( String path ) {
		Map<String, String> locations = new HashMap<>();

		if ( isActive() ) {
			for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
				String resourceKey = moduleInfo.getResourcesKey();

				String location = moduleResourcePaths.get( moduleInfo.getName() );

				if ( location != null ) {
					Path resourcePath = Paths.get( location ).resolve( path ).resolve( resourceKey );

					if ( resourcePath.toFile().exists() ) {
						locations.put( resourceKey, resourcePath.toString() );
					}
				}
			}
		}

		return locations;
	}
}
