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
package com.foreach.across.test.support;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.MockAcrossServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import java.util.Collection;

/**
 * Extends the default {@link MockMvcBuilders} with support for creating a {@link DefaultMockMvcBuilder}
 * for a fully bootstrapped {@link com.foreach.across.core.AcrossContext}.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class AcrossMockMvcBuilders extends MockMvcBuilders
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossMockMvcBuilders.class );

	/**
	 * Build a {@link MockMvc} using the given, fully bootstrapped {@link AcrossContext}.
	 * The {@link DispatcherServlet} will use the context to discover Spring MVC infrastructure and
	 * application controllers in it. The context must have been configured with
	 * a {@link ServletContext}, ideally a {@link com.foreach.across.test.MockAcrossServletContext}.
	 */
	public static DefaultMockMvcBuilder acrossContextSetup( AcrossContext acrossContext ) {
		return acrossContextSetup( AcrossContextUtils.getContextInfo( acrossContext ) );
	}

	/**
	 * Build a {@link MockMvc} using the given, fully bootstrapped {@link AcrossContext}.
	 * The {@link DispatcherServlet} will use the context to discover Spring MVC infrastructure and
	 * application controllers in it. The context must have been configured with
	 * a {@link ServletContext}, ideally a {@link com.foreach.across.test.MockAcrossServletContext}.
	 */
	public static DefaultMockMvcBuilder acrossContextSetup( AcrossTestContext acrossTestContext ) {
		return acrossContextSetup( acrossTestContext.contextInfo() );
	}

	/**
	 * Build a {@link MockMvc} using the given, fully bootstrapped {@link AcrossContext}.
	 * The {@link DispatcherServlet} will use the context to discover Spring MVC infrastructure and
	 * application controllers in it. The context must have been configured with
	 * a {@link ServletContext}, ideally a {@link com.foreach.across.test.MockAcrossServletContext}.
	 */
	public static DefaultMockMvcBuilder acrossContextSetup( AcrossContextInfo acrossContextInfo ) {
		WebApplicationContext wac = webApplicationContext( acrossContextInfo );
		DefaultMockMvcBuilder mockMvcBuilder = MockMvcBuilders.webAppContextSetup( wac );

		ServletContext servletContext = wac.getServletContext();
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

			// Set the web application context attribute to AcrossContext anyway for maximum functionality
			servletContext.setAttribute(
					WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
					acrossContextInfo.getApplicationContext()
			);
		}

		return mockMvcBuilder;
	}

	private static WebApplicationContext webApplicationContext( AcrossContextInfo contextInfo ) {
		ApplicationContext acrossApplicationContext = contextInfo.getApplicationContext();

		if ( acrossApplicationContext instanceof WebApplicationContext ) {
			return (WebApplicationContext) acrossApplicationContext;
		}

		return (WebApplicationContext) acrossApplicationContext.getParent();
	}
}
