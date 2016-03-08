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

import com.foreach.across.test.MockAcrossServletContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link ApplicationContextInitializer} to be used in conjunction with
 * {@link org.springframework.test.context.web.WebAppConfiguration}.  This initializer will configure a
 * {@link MockAcrossServletContext} instead of the default {@link org.springframework.mock.web.MockServletContext}.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class MockAcrossServletContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext>
{
	@Override
	public void initialize( ConfigurableWebApplicationContext applicationContext ) {
		MockAcrossServletContext servletContext = new MockAcrossServletContext( true );
		applicationContext.setServletContext( servletContext );
		applicationContext.addApplicationListener( new ContextRefreshListener( applicationContext, servletContext ) );
	}

	/**
	 * Ensures that {@link MockAcrossServletContext#initialize()} is called once the
	 * {@link org.springframework.context.ApplicationContext} is refreshed.  This should initialize all
	 * registered filters and servlets.
	 */
	private static class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent>
	{
		private final WebApplicationContext wac;
		private final MockAcrossServletContext servletContext;

		public ContextRefreshListener( WebApplicationContext wac,
		                               MockAcrossServletContext servletContext ) {
			this.wac = wac;
			this.servletContext = servletContext;
		}

		@Override
		public void onApplicationEvent( ContextRefreshedEvent event ) {
			if ( event.getApplicationContext() == wac ) {
				servletContext.initialize();
			}
		}
	}
}
