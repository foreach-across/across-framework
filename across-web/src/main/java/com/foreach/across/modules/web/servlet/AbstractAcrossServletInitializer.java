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

import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Initializes a single dispatchers servlet with a root Application context that is initialized upon creation.
 * This allows any modules in the root Application context to extend the ServletContext.
 * <p>
 * Servlet 3 environments only need to extend this initializer and configure the application context with
 * one or more configuration classes or locations.  Any AcrossContext bootstrapped in the ApplicationContext
 * will be able to extend the ServletContext.</p>
 *
 * @author Arne Vandamme
 * @see
 */
public abstract class AbstractAcrossServletInitializer extends AbstractDispatcherServletInitializer
{
	/**
	 * Attribute set on the {@link ServletContext} during the initialization phase.  Can be used to detect
	 * if it is still possible to register servlets or filters on the context.
	 */
	public static final String DYNAMIC_INITIALIZER = "com.foreach.across.modules.web.servlet.AcrossServletInitializer";
	public static final String ATTRIBUTE_DYNAMIC_MULTIPART_CONFIG =
			"com.foreach.across.modules.web.servlet.AcrossServletInitializer.MultiPartConfigElement";

	private ServletContext servletContext;

	@Override
	public void onStartup( ServletContext servletContext ) throws ServletException {
		this.servletContext = servletContext;

		super.onStartup( servletContext );

		extendServletContext( servletContext );
	}

	@SuppressWarnings( "all" )
	@Override
	protected void registerContextLoaderListener( ServletContext servletContext ) {
		WebApplicationContext rootAppContext = createRootApplicationContext();
		if ( rootAppContext != null ) {
			servletContext.setAttribute( DYNAMIC_INITIALIZER, this );

			ContextLoaderListener listener = new ShutdownOnlyContextLoaderListener( rootAppContext );
			listener.initWebApplicationContext( servletContext );
			servletContext.removeAttribute( DYNAMIC_INITIALIZER );

			servletContext.addListener( listener );
		}
		else {
			logger.debug( "No ContextLoaderListener registered, as " +
					              "createRootApplicationContext() did not return an application context" );
		}
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AcrossWebApplicationContext context = new AcrossWebApplicationContext();
		configure( context );

		return context;
	}

	/**
	 * Configure the ApplicationContext before it is initialized.
	 *
	 * @param applicationContext AcrossWebApplicationContext that contains the main Across context configuration.
	 */
	protected abstract void configure( AcrossWebApplicationContext applicationContext );

	/**
	 * Creates a default empty WebApplicationContext for the servlet itself.
	 */
	@Override
	protected WebApplicationContext createServletApplicationContext() {
		return new AcrossWebApplicationContext();
	}

	@Override
	protected String[] getServletMappings() {
		// All paths are mapped to this dispatcher servlet
		return new String[] { "/" };
	}

	/**
	 * Extension point for extending the ServletContext during initialization.
	 *
	 * @param servletContext ServletContext while initialization is busy.
	 */
	protected void extendServletContext( ServletContext servletContext ) {
	}

	@Override
	protected void customizeRegistration( ServletRegistration.Dynamic registration ) {
		MultipartConfigElement multipartConfigElement = (MultipartConfigElement) servletContext.getAttribute(
				ATTRIBUTE_DYNAMIC_MULTIPART_CONFIG );

		if ( multipartConfigElement != null ) {
			registration.setMultipartConfig( multipartConfigElement );

			servletContext.removeAttribute( ATTRIBUTE_DYNAMIC_MULTIPART_CONFIG );
		}
	}
}
