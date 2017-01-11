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
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.config.resources.ResourceConfigurationProperties;
import com.foreach.across.modules.web.context.PrefixingPathContext;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Collections;

/**
 * Registers the {@link com.foreach.across.modules.web.context.PrefixingPathRegistry}
 * and inserts a filter to ensure that {@link javax.servlet.http.HttpServletResponse#encodeURL(String)} also
 * supports named prefixes.
 * <p/>
 * Two default prefixes are registered:
 * <ul>
 * <li><strong>resource</strong>: will resolve to the general resources path</li>
 * <li><strong>static</strong>: will resolve to the static folder under general resources,
 * short-hand for <em>@resource:/static/&lt;path&gt;</em></li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Configuration
public class UrlPrefixingConfiguration
{
	public static final String PATH_RESOLVING_URL_ENCODING_FILTER = "pathResolvingUrlEncodingFilter";

	public static final String RESOURCE = "resource";
	public static final String STATIC = "static";

	@Bean
	@Primary
	@Exposed
	public PrefixingPathRegistry prefixingPathRegistry( ResourceConfigurationProperties resourcesConfiguration ) {
		PrefixingPathRegistry prefixingPathRegistry = new PrefixingPathRegistry();
		PrefixingPathContext resourceContext = new PrefixingPathContext( resourcesConfiguration.getPath() );
		prefixingPathRegistry.add( RESOURCE, resourceContext );
		prefixingPathRegistry.add( STATIC, new PrefixingPathContext( resourceContext.getPathPrefix() + "/static" ) );
		return prefixingPathRegistry;
	}

	@Bean
	@ConditionalOnConfigurableServletContext
	public FilterRegistrationBean prefixingPathUrlEncodingFilterRegistrationBean( PrefixingPathRegistry prefixingPathRegistry ) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName( PATH_RESOLVING_URL_ENCODING_FILTER );
		registration.setFilter( new PathResolvingUrlEncodingFilter( prefixingPathRegistry ) );
		registration.setAsyncSupported( true );
		registration.setMatchAfter( true );
		registration.setUrlPatterns( Collections.singletonList( "/*" ) );
		registration.setDispatcherTypes( DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC );
		registration.setOrder( Ordered.LOWEST_PRECEDENCE - 9 );

		return registration;
	}

	/**
	 * Filter that wraps the {@link HttpServletResponse} so the
	 * {@link javax.servlet.http.HttpServletResponse#encodeURL(String)} method gets overwritten.
	 */
	public static class PathResolvingUrlEncodingFilter extends GenericFilterBean
	{
		private final WebAppPathResolver webAppPathResolver;

		PathResolvingUrlEncodingFilter( WebAppPathResolver webAppPathResolver ) {
			this.webAppPathResolver = webAppPathResolver;
		}

		@Override
		public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain )
				throws IOException, ServletException {
			if ( !( request instanceof HttpServletRequest ) || !( response instanceof HttpServletResponse ) ) {
				throw new ServletException( "PathResolvingUrlEncodingFilter just supports HTTP requests" );
			}
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			filterChain.doFilter(
					httpRequest,
					new PathResolvingUrlEncodingResponseWrapper( httpResponse, webAppPathResolver )
			);
		}

		/**
		 * Wrapper to dispatch a url to the {@link WebAppPathResolver} before actually encoding it.
		 */
		private static class PathResolvingUrlEncodingResponseWrapper extends HttpServletResponseWrapper
		{
			private final WebAppPathResolver webAppPathResolver;

			PathResolvingUrlEncodingResponseWrapper( HttpServletResponse wrapped,
			                                         WebAppPathResolver webAppPathResolver ) {
				super( wrapped );
				Assert.notNull( webAppPathResolver );
				this.webAppPathResolver = webAppPathResolver;
			}

			@Override
			public String encodeURL( String url ) {
				return super.encodeURL( webAppPathResolver.path( url ) );
			}
		}
	}
}
