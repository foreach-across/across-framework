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
package com.foreach.across.test.web;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.multipart.MultipartResolverConfiguration;
import com.foreach.across.test.AcrossTestWebContext;
import com.foreach.across.test.MockFilterRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestMultipartResolverConfiguration
{
	@Test
	public void defaultMultipartFilter() {
		try (AcrossTestWebContext ctx = web().modules( AcrossWebModule.NAME ).build()) {
			MockFilterRegistration registration
					= ctx.getServletContext().getFilterRegistration( MultipartResolverConfiguration.FILTER_NAME );

			assertNotNull( registration );

			MultipartResolver multipartResolver = ctx.getBeanFromModule( AcrossWebModule.NAME, "filterMultipartResolver" );
			assertNotNull( multipartResolver );

			MultipartConfigElement configElement = ctx.getBeanOfType( MultipartConfigElement.class );
			assertNotNull( configElement );

			assertSame(
					configElement,
					ctx.getBeanOfTypeFromModule( AcrossWebModule.NAME, MultipartConfigElement.class )
			);
		}
	}

	@Test
	public void customMultipartConfiguration() {
		try (AcrossTestWebContext ctx = web().register( CustomMultipartConfiguration.class )
		                                     .modules( AcrossWebModule.NAME )
		                                     .build()) {
			MultipartConfigElement configElement = ctx.getBeanOfType( MultipartConfigElement.class );
			assertNotNull( configElement );

			assertSame(
					configElement,
					ctx.getBeanOfTypeFromModule( AcrossWebModule.NAME, MultipartConfigElement.class )
			);
		}
	}

	@Test
	public void defaultMultipartFilterWithExistingResolver() throws Exception {
		try (AcrossTestWebContext ctx = web().register( CustomFilterMultipartResolver.class )
		                                     .modules( AcrossWebModule.NAME )
		                                     .build()) {
			MockFilterRegistration registration
					= ctx.getServletContext().getFilterRegistration( MultipartResolverConfiguration.FILTER_NAME );
			assertNotNull( registration );

			MultipartResolver multipartResolver = ctx.getBean( "filterMultipartResolver", MultipartResolver.class );
			assertNotNull( multipartResolver );

			Filter multipartFilter = registration.getFilter();
			HttpServletRequest request = mock( HttpServletRequest.class );
			when( request.getDispatcherType() ).thenReturn( DispatcherType.REQUEST );
			HttpServletResponse response = mock( HttpServletResponse.class );

			multipartFilter.doFilter( request, response, mock( FilterChain.class ) );

			verify( multipartResolver ).isMultipart( request );
		}
	}

	@Test
	public void multipartResolverWithRightNameWillCauseFilterNotToBeCreated() {
		try (AcrossTestWebContext ctx = web().register( CustomMultipartResolver.class )
		                                     .modules( AcrossWebModule.NAME )
		                                     .build()) {
			assertNull( ctx.getServletContext().getFilterRegistration( MultipartResolverConfiguration.FILTER_NAME ) );
		}
	}

	@Configuration
	protected static class CustomMultipartResolver
	{
		@Bean
		public MultipartResolver multipartResolver() {
			return mock( MultipartResolver.class );
		}
	}

	@Configuration
	protected static class CustomFilterMultipartResolver
	{
		@Bean
		public MultipartResolver filterMultipartResolver() {
			return mock( MultipartResolver.class );
		}
	}

	@Configuration
	protected static class CustomMultipartConfiguration
	{
		@Bean
		public MultipartConfigElement multipartConfigElement() {
			return mock( MultipartConfigElement.class );
		}
	}
}
