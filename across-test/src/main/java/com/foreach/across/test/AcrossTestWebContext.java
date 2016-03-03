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
package com.foreach.across.test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

/**
 * Extends the default AcrossTextContext by creating a WebApplicationContext,
 * MockServletContext and adding AcrossWebModule to the configuration.  An
 * AcrossTestWebContext has the same result as using the {@link com.foreach.across.test.AcrossTestWebConfiguration}
 * annotation.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.test.support.AcrossTestBuilders
 * @see com.foreach.across.test.support.AcrossTestWebContextBuilder
 */
public class AcrossTestWebContext extends AcrossTestContext
{
	private MockAcrossServletContext servletContext;
	private MockMvc mockMvc;

	private ApplicationContext acrossApplicationContext;

	/**
	 * @param configurers list of configures
	 * @deprecated use {@link com.foreach.across.test.support.AcrossTestBuilders} instead
	 */
	@Deprecated
	public AcrossTestWebContext( AcrossContextConfigurer... configurers ) {
		super( configurers );
	}

	/**
	 * Retrieve the {@link MockAcrossServletContext} that was set.
	 * If dynamic registration was enabled, the instance can be queried for servlet registrations.
	 *
	 * @return servletContext instance
	 */
	public MockAcrossServletContext getServletContext() {
		return servletContext;
	}

	protected void setServletContext( MockAcrossServletContext servletContext ) {
		this.servletContext = servletContext;
	}

	@Override
	protected void setAcrossContext( AcrossContext acrossContext ) {
		super.setAcrossContext( acrossContext );

		acrossApplicationContext = AcrossContextUtils.getApplicationContext( acrossContext );
	}

	@Override
	protected void setApplicationContext( ConfigurableApplicationContext applicationContext ) {
		super.setApplicationContext( applicationContext );
	}

	@Override
	protected AcrossConfigurableApplicationContext createApplicationContext() {
		AcrossWebApplicationContext wac = new AcrossWebApplicationContext();
		wac.register( AcrossTestWebContextConfiguration.class );

		servletContext = new MockAcrossServletContext();
		wac.setServletContext( servletContext );

		return wac;
	}

	/**
	 * Returns an initialized {@link MockMvc} for the internal {@link com.foreach.across.core.AcrossContext}.
	 * If the attached {@link MockAcrossServletContext} has {@link MockAcrossServletContext#isDynamicRegistrationAllowed()}
	 * {@code true}, registered filters will be added to this {@link MockMvc} instance.
	 *
	 * @return instance ready for mock requests
	 */
	public MockMvc mockMvc() {
		if ( mockMvc == null ) {
			MockAcrossServletContext servletContext = getServletContext();

			WebApplicationContext wac = webApplicationContext();
			DefaultMockMvcBuilder mockMvcBuilder = MockMvcBuilders.webAppContextSetup( wac );

			if ( servletContext.isDynamicRegistrationAllowed() ) {
				servletContext.getFilterRegistrations()
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

			mockMvc = mockMvcBuilder.build();
		}

		return mockMvc;
	}

	private WebApplicationContext webApplicationContext() {
		if ( acrossApplicationContext instanceof WebApplicationContext ) {
			return (WebApplicationContext) acrossApplicationContext;
		}

		return (WebApplicationContext) acrossApplicationContext.getParent();
	}
}
