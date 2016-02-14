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
package com.foreach.across.modules.web.config;

import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;

/**
 * Registers a default {@link org.springframework.web.filter.CharacterEncodingFilter} as the very first
 * filter in the chain.  It is vital that this filter comes first so the encoding is applied as early as possible.
 * <p>
 * This configuration reuses {@link HttpEncodingAutoConfiguration} and corresponding
 * {@link org.springframework.boot.autoconfigure.web.HttpEncodingProperties}.
 *
 * @author Arne Vandamme
 * @see HttpEncodingAutoConfiguration
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@Import(HttpEncodingAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.http.encoding", value = "enabled", matchIfMissing = true)
public class CharacterEncodingConfiguration extends AcrossWebDynamicServletConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger( CharacterEncodingConfiguration.class );

	@Autowired
	private CharacterEncodingFilter characterEncodingFilter;

	@Override
	protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
		FilterRegistration.Dynamic registration
				= servletContext.addFilter( "characterEncodingFilter", characterEncodingFilter );

		registration.addMappingForUrlPatterns(
				EnumSet.of( DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR ),
				false,
				"/*"
		);
	}

	@Override
	protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		LOG.warn( "Unable to auto register the character encoding filter." );
	}
}
