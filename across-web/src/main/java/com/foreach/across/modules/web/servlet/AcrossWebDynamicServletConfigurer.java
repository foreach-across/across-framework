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

package com.foreach.across.modules.web.servlet;

import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Base configurer class that can be used to dynamically extend a ServletContext.
 * Works by checking if the {@link com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer}
 * was used to bootstrap the context.
 * <p/>
 * Since {@code 2.0.0} the use of {@link ServletContextInitializer} beans directly inside modules is also supported
 * and preferred.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer
 * @see ServletContextInitializer
 * @deprecated in favour of {@link org.springframework.boot.web.servlet.FilterRegistrationBean} or {@link org.springframework.boot.web.servlet.ServletRegistrationBean}
 */
@Deprecated
public abstract class AcrossWebDynamicServletConfigurer implements ServletContextInitializer
{
	@Override
	public final void onStartup( ServletContext servletContext ) throws ServletException {
		configure( servletContext, Boolean.TRUE.equals(
				servletContext.getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER )
		) );
	}

	/**
	 * Apply the configuration to the {@link ServletContext} provided.  Will dispatch to either
	 * {@link #dynamicConfigurationAllowed(ServletContext)} or {@link #dynamicConfigurationDenied(ServletContext)}
	 * depending on the changeable state of the {@link ServletContext} itself.
	 *
	 * @param servletContext             in which to register the servlets (if possible)
	 * @param servletContextIsExtensible {@code true} if the context can still have filters or servlets registered
	 * @throws ServletException if an error occurs
	 */
	public final void configure( ServletContext servletContext,
	                             boolean servletContextIsExtensible ) throws ServletException {
		if ( servletContextIsExtensible ) {
			dynamicConfigurationAllowed( servletContext );
		}
		else {
			dynamicConfigurationDenied( servletContext );
		}
	}

	/**
	 * Called when the ServletContext initialization is still busy and dynamic extension should be possible.
	 *
	 * @param servletContext ServletContext that is being initialized.
	 */
	protected abstract void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException;

	/**
	 * Called when the ServletContext does not allow dynamic extension.  Probably because
	 * the ApplicationContext was not started using an {@link com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer}.
	 * <p>
	 * Usually means this configurer can not do its part and some feedback should be presented to the user.</p>
	 *
	 * @param servletContext ServletContext that already is initialized.
	 */
	protected abstract void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException;
}
