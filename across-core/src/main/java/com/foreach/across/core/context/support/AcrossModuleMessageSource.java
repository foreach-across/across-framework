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
package com.foreach.across.core.context.support;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>Extension of {@link org.springframework.context.support.ReloadableResourceBundleMessageSource}.
 * If no {@link #baseNames} are specified, this implementation will configure a message source for the
 * Across conventional resource bundle locations:
 * <ul>
 * <li>resources/messages/MODULE_RESOURCES/MODULE_NAME.properties</li>
 * <li>resources/messages/MODULE_RESOURCES/default.properties</li>
 * <li>resources/messages/MODULE_RESOURCES/default/*.properties</li>
 * </ul></p>
 * <p>
 * If {@link com.foreach.across.core.development.AcrossDevelopmentMode} is active, messages will be configured
 * to be loaded from the physical path with a cacheRefresh of 1 second.  In that case all basenames
 * of the form {@code classpath:/messages/MODULE_RESOURCE/} will be replaced with the physical path if it exists.
 * </p>
 *
 * @author Arne Vandamme
 */
public class AcrossModuleMessageSource extends ReloadableResourceBundleMessageSource
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossModuleMessageSource.class );

	@Autowired
	private AcrossDevelopmentMode developmentMode;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AcrossModule currentModule;

	private String[] baseNames = new String[0];

	public AcrossModuleMessageSource() {
		setUseCodeAsDefaultMessage( true );
		setDefaultEncoding( "UTF-8" );
	}

	@PostConstruct
	protected void createDefaultAndSetUpDevelopmentMode() {
		String basePath = "classpath:/messages/" + currentModule.getResourcesKey();

		if ( baseNames == null || baseNames.length == 0 ) {
			registerDefaultMessageSources();
		}

		if ( developmentMode.isActive() ) {
			setCacheSeconds( 1 );

			String physicalPath = developmentMode.getDevelopmentLocationForResourcePath( currentModule, "messages" );

			if ( physicalPath != null ) {
				physicalPath = "file:" + physicalPath;

				LOG.info( "Mapping resource bundle paths {} to physical {}", basePath, physicalPath );
				String[] replaced = new String[baseNames.length];

				for ( int i = 0; i < baseNames.length; i++ ) {
					replaced[i] = StringUtils.replace( baseNames[i], basePath, physicalPath );
				}

				setBasenames( replaced );
			}
		}
	}

	private void registerDefaultMessageSources() {
		Set<String> baseNames = new TreeSet<>();

		// initial default
		String basePath = "/messages/" + currentModule.getResourcesKey();
		baseNames.add( ResourcePatternResolver.CLASSPATH_URL_PREFIX + basePath + "/" + currentModule.getName() );
		baseNames.add( ResourcePatternResolver.CLASSPATH_URL_PREFIX + basePath + "/default" );

		// additional defaults
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		String additionalDefaultSources = basePath + "/default/";

		String resourcesSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath( additionalDefaultSources ) + "*.properties";

		try {
			Resource[] resources = resourcePatternResolver.getResources( resourcesSearchPath );

			for ( Resource resource : resources ) {
				String fileName = resource.getFilename();
				String baseName = StringUtils.contains( fileName, "_" )
						? StringUtils.substringBefore( fileName, "_" )
						: StringUtils.substringBeforeLast( fileName, "." );

				baseNames.add( ResourceLoader.CLASSPATH_URL_PREFIX + additionalDefaultSources + baseName );
			}
		}
		catch ( IOException ioe ) {
			LOG.warn( "Unable to read message resources", ioe );
		}

		if ( LOG.isTraceEnabled() ) {
			baseNames.forEach(
					baseName -> LOG.trace( "Registering default message source for module {}: {}",
					                       currentModule.getName(), baseName )
			);
		}

		setBasenames( baseNames.toArray( new String[baseNames.size()] ) );
	}

	@Override
	public void setBasenames( String... baseNames ) {
		super.setBasenames( baseNames );

		this.baseNames = baseNames;
	}
}
