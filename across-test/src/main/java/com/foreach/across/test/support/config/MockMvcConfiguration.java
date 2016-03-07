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
package com.foreach.across.test.support.config;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.test.MockAcrossServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

/**
 * Configures a {@link MockMvc} bean that supports testing a bootstrapped {@link com.foreach.across.core.AcrossContext}
 * with the registered filters of a {@link MockAcrossServletContext}.
 * <p>
 * This requires that the created {@link javax.servlet.ServletContext} is of type {@link MockAcrossServletContext}.
 * If the latter is not the case, a standard configured {@link MockMvc} instance will still be available,
 * but an error message will be logged.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.test.AcrossWebAppConfiguration
 * @since 1.1.2
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnBean(MockServletContext.class)
public class MockMvcConfiguration
{
	public static final Logger LOG = LoggerFactory.getLogger( MockMvcConfiguration.class );

	@Bean
	@Lazy
	public MockMvc mockMvc( MockServletContext servletContext, AcrossContextInfo contextInfo ) {
		servletContext.setAttribute(
				WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				contextInfo.getApplicationContext()
		);

		WebApplicationContext wac = webApplicationContext( contextInfo );
		DefaultMockMvcBuilder mockMvcBuilder = MockMvcBuilders.webAppContextSetup( wac );

		if ( servletContext instanceof MockAcrossServletContext ) {
			MockAcrossServletContext sc = (MockAcrossServletContext) servletContext;
			sc.getFilterRegistrations()
			  .values()
			  .stream()
			  .filter( r -> r.getFilter() != null )
			  .forEach( r -> {
				  Collection<String> urlPatternMappings = r.getUrlPatternMappings();
				  mockMvcBuilder.addFilter(
						  r.getFilter(),
						  urlPatternMappings.toArray( new String[urlPatternMappings.size()] )
				  );
			  } );
		}
		else {
			LOG.error( "Creating a MockMvc instance but impossible to add dynamically registered filters" +
					           " as the ServletContext is not a MockAcrossServletContext." );
			LOG.error( "Did you forget to annotate your test class with @AcrossWebAppConfiguration?" );
		}

		return mockMvcBuilder.build();
	}

	private WebApplicationContext webApplicationContext( AcrossContextInfo contextInfo ) {
		ApplicationContext acrossApplicationContext = contextInfo.getApplicationContext();

		if ( acrossApplicationContext instanceof WebApplicationContext ) {
			return (WebApplicationContext) acrossApplicationContext;
		}

		return (WebApplicationContext) acrossApplicationContext.getParent();
	}
}
