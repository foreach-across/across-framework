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

package com.foreach.across.modules.web.mvc;

import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.*;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("all")
public class ResourceHandlerRegistry extends org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
{
	private String version;
	private Boolean autoConfigure;

	public ResourceHandlerRegistry( ApplicationContext applicationContext,
	                                ServletContext servletContext,
	                                AcrossModuleInfo currentModuleInfo ) {
		super( applicationContext, servletContext );
		setVersion( applicationContext, currentModuleInfo );
	}

	private void setVersion( ApplicationContext applicationContext, AcrossModuleInfo currentModuleInfo ) {
		version = applicationContext.getEnvironment().getProperty( AcrossWebModuleSettings.RESOURCES_VERSION );
		autoConfigure = applicationContext.getEnvironment().getProperty(
				AcrossWebModuleSettings.RESOURCE_URLS_AUTO_CONFIGURE, Boolean.class );
		if ( StringUtils.isEmpty( version ) ) {
			version = applicationContext.getEnvironment().getProperty( "build.number" );
			if ( StringUtils.isEmpty( version ) ) {
				version = currentModuleInfo.getVersionInfo().getVersion();
			}
		}
	}

	public Map<String, ?> getUrlMap() {
		AbstractHandlerMapping mapping = getHandlerMapping();

		if ( mapping instanceof SimpleUrlHandlerMapping ) {
			if ( autoConfigure && StringUtils.isNotEmpty( version ) ) {
				Map<String, ?> entries = ( (SimpleUrlHandlerMapping) mapping ).getUrlMap();
				for ( Map.Entry<String, ?> entry : entries.entrySet() ) {
					if ( entry.getValue() instanceof ResourceHttpRequestHandler ) {
						ResourceHttpRequestHandler handler = (ResourceHttpRequestHandler) entry.getValue();
						if ( handler.getResourceResolvers().size() == 1 && handler.getResourceResolvers().get(
								0 ) instanceof PathResourceResolver && handler.getResourceTransformers().isEmpty() ) {
							//TODO: AX-56 improve this check without rewriting all spring classes?
							AppCacheManifestTransformer appCacheTransformer = new AppCacheManifestTransformer();
							VersionResourceResolver versionResolver = new VersionResourceResolver()
									.addVersionStrategy( new FixedVersionStrategy( version ), "/**" );
							handler.setResourceResolvers( Arrays.asList( versionResolver,
							                                             new PathResourceResolver() ) );
							handler.setResourceTransformers( Arrays.asList( appCacheTransformer,
							                                                new CssLinkResourceTransformer() ) );
							try {
								handler.afterPropertiesSet();
							}
							catch ( Exception e ) {
								throw new BeanInitializationException( "Failed to init ResourceHttpRequestHandler", e );
							}
						}
					}
				}
				return entries;
			}
			else {
				return ( (SimpleUrlHandlerMapping) mapping ).getUrlMap();
			}

		}

		return Collections.emptyMap();
	}
}
