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
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.modules.web.context.AcrossWebApplicationContext;

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
	protected AcrossConfigurableApplicationContext createApplicationContext() {
		AcrossWebApplicationContext wac = new AcrossWebApplicationContext();
		wac.register( AcrossTestWebContextConfiguration.class );

		servletContext = new MockAcrossServletContext();
		wac.setServletContext( servletContext );

		return wac;
	}
}
