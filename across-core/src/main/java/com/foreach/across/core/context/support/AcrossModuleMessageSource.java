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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Extension of {@link org.springframework.context.support.ReloadableResourceBundleMessageSource} that implements
 * the convention of Across resource bundle locations.  It assumes a message source for the current module is
 * being requested that is present in the conventional location (eg: resources/messages/MODULE_RESOURCES/).
 * <p/>
 * If {@link com.foreach.across.core.development.AcrossDevelopmentMode} is active, messages will be configured
 * to be loaded from the physical path with a cacheRefresh of 1 second.
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
	}

	@PostConstruct
	protected void createDefaultAndSetUpDevelopmentMode() {
		String basePath = "classpath:/messages/" + currentModule.getResourcesKey();

		if ( baseNames == null || baseNames.length == 0 ) {
			String defaultResourceBundle = basePath + "/" + currentModule.getName();
			LOG.trace( "Registering default message source {}", defaultResourceBundle );
			setBasename( defaultResourceBundle );
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

	@Override
	public void setBasenames( String... baseNames ) {
		super.setBasenames( baseNames );

		this.baseNames = baseNames;
	}
}
