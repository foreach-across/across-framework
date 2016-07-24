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
import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import com.foreach.across.test.support.AcrossMockMvcBuilders;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/**
 * Extends the default {@link AcrossTestContext} with support for web configurations.
 * This class assumes that the configured {@link AcrossContext} was configured using
 * a {@link MockAcrossServletContext} and with {@link WebApplicationContext} support.
 * <p>
 * Instances of this class should not be created manually but through one of the builders.
 * See {@link com.foreach.across.test.support.AcrossTestBuilders}.
 * Public constructors will be removes in a future release.
 * </p>
 *
 * @author Arne Vandamme
 * @see com.foreach.across.test.support.AcrossTestBuilders
 * @see com.foreach.across.test.support.AcrossTestWebContextBuilder
 */
public class AcrossTestWebContext extends AcrossTestContext
{
	private MockAcrossServletContext servletContext;
	private MockMvc mockMvc;

	protected AcrossTestWebContext() {
	}

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

	/**
	 * Returns an initialized {@link MockMvc} for the internal {@link com.foreach.across.core.AcrossContext}.
	 * All filters registered on the {@link MockAcrossServletContext} will be added to this {@link MockMvc} instance.
	 *
	 * @return instance ready for mock requests
	 */
	public MockMvc mockMvc() {
		if ( mockMvc == null ) {
			mockMvc = AcrossMockMvcBuilders.acrossContextSetup( this ).build();
		}

		return mockMvc;
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
}
