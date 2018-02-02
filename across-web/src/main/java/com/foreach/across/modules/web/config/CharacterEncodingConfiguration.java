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

import com.foreach.across.condition.ConditionalOnConfigurableServletContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;
import java.util.Collections;

/**
 * Registers a default {@link org.springframework.web.filter.CharacterEncodingFilter} as the very first
 * filter in the chain.  It is vital that this filter comes first so the encoding is applied as early as possible.
 * <p>
 * This configuration reuses {@link HttpEncodingAutoConfiguration} and corresponding
 * {@link org.springframework.boot.autoconfigure.http.HttpEncodingProperties}.
 *
 * @author Arne Vandamme
 * @see HttpEncodingAutoConfiguration
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@Import(HttpEncodingAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.http.encoding", value = "enabled", matchIfMissing = true)
public class CharacterEncodingConfiguration
{
	public static final String FILTER_NAME = "characterEncodingFilter";

	@Bean
	@ConditionalOnConfigurableServletContext
	public FilterRegistrationBean characterEncodingFilterRegistration( CharacterEncodingFilter characterEncodingFilter ) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName( FILTER_NAME );
		registration.setFilter( characterEncodingFilter );
		registration.setAsyncSupported( true );
		registration.setMatchAfter( false );
		registration.setUrlPatterns( Collections.singletonList( "/*" ) );
		registration.setDispatcherTypes( DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC );
		registration.setOrder( Ordered.HIGHEST_PRECEDENCE );

		return registration;
	}
}
