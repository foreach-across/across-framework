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
package com.foreach.across.modules.web.config.resourceurls;

import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;

@Configuration
@ConditionalOnProperty(value = "acrossWebModule.resources.versioning", matchIfMissing = true)
public class ResourceUrlEncodingFilterConfiguration extends AcrossWebDynamicServletConfigurer
{
	public static final String FILTER_NAME = "ResourceUrlEncodingFilter";

	private static final Logger LOG = LoggerFactory.getLogger( ResourceUrlEncodingFilterConfiguration.class );

	@Override
	protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
		FilterRegistration.Dynamic resourceUrlEncodingFilter = servletContext.addFilter( FILTER_NAME,
		                                                                                 new ResourceUrlEncodingFilter() );
		resourceUrlEncodingFilter.addMappingForUrlPatterns( EnumSet.of(
				                                                    DispatcherType.REQUEST,
				                                                    DispatcherType.ERROR,
				                                                    DispatcherType.ASYNC
		                                                    ),
		                                                    false,
		                                                    "/*" );
	}

	@Override
	protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		LOG.warn( "Unable to auto configure ResourceUrlEncodingFilter." );
	}
}
