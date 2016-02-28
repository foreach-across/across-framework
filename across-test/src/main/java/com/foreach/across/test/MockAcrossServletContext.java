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

import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.*;

/**
 * Extension of the Spring {@link MockServletContext} that also keeps track of all registration actions that
 * have occurred on servlets, filters and listeners.  While they will never work in a mock mvc scenario, the
 * {@link MockAcrossServletContext} can be queried to verify registration was done correctly.
 * <p>
 * By setting {@link #setDynamicRegistrationAllowed(boolean)} to {@code true} this {@link MockServletContext}
 * can mimic a dynamically extensible {@link javax.servlet.ServletContext} (that has not yet been initialized).
 * If allowed, any {@link com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer} bean in the
 * {@link com.foreach.across.core.AcrossContext} will execute its configuration.
 * <p>
 * This is also the default behaviour when creating a new {@link MockAcrossServletContext}.
 *
 * @author Marc Vanbrabant, Arne Vandamme
 */
public class MockAcrossServletContext extends MockServletContext
{
	private final Map<String, MockFilterRegistration> filters = new LinkedHashMap<>();
	private final Map<String, MockServletRegistration> servlets = new LinkedHashMap<>();
	private final List<Object> listeners = new ArrayList<>();

	public MockAcrossServletContext() {
		this( true );
	}

	public MockAcrossServletContext( boolean dynamicRegistrationAllowed ) {
		setDynamicRegistrationAllowed( dynamicRegistrationAllowed );
	}

	/**
	 * @param allowed true if servlet context should allow extensions (servlet & filter registrations)
	 */
	public void setDynamicRegistrationAllowed( boolean allowed ) {
		if ( !allowed ) {
			removeAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER );
		}
		else {
			setAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER, true );
		}
	}

	/**
	 * @return true if the servlet context acts as not yet fully initialized
	 */
	public boolean isDynamicRegistrationAllowed() {
		return getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER ) != null;
	}

	@Override
	public MockServletRegistration addServlet( String servletName, Class<? extends Servlet> servletClass ) {
		return servletRegistration( new MockServletRegistration( servletName, servletClass ) );
	}

	@Override
	public MockServletRegistration addServlet( String servletName, String className ) {
		return servletRegistration( new MockServletRegistration( servletName, className ) );
	}

	@Override
	public MockServletRegistration addServlet( String servletName, Servlet servlet ) {
		return servletRegistration( new MockServletRegistration( servletName, servlet ) );
	}

	@Override
	public MockServletRegistration getServletRegistration( String servletName ) {
		return servlets.get( servletName );
	}

	@Override
	public Map<String, MockServletRegistration> getServletRegistrations() {
		return Collections.unmodifiableMap( servlets );
	}

	@Override
	public MockFilterRegistration addFilter( String filterName, String className ) {
		return filterRegistration( new MockFilterRegistration( filterName, className ) );
	}

	@Override
	public MockFilterRegistration addFilter( String filterName, Filter filter ) {
		return filterRegistration( new MockFilterRegistration( filterName, filter ) );
	}

	@Override
	public MockFilterRegistration addFilter( String filterName, Class<? extends Filter> filterClass ) {
		return filterRegistration( new MockFilterRegistration( filterName, filterClass ) );
	}

	@Override
	public MockFilterRegistration getFilterRegistration( String filterName ) {
		return filters.get( filterName );
	}

	@Override
	public Map<String, MockFilterRegistration> getFilterRegistrations() {
		return Collections.unmodifiableMap( filters );
	}

	private MockFilterRegistration filterRegistration( MockFilterRegistration registration ) {
		if ( filters.containsKey( registration.getName() ) ) {
			return filters.get( registration.getName() );
		}

		filters.put( registration.getName(), registration );
		return registration;
	}

	private MockServletRegistration servletRegistration( MockServletRegistration registration ) {
		if ( servlets.containsKey( registration.getName() ) ) {
			return servlets.get( registration.getName() );
		}

		servlets.put( registration.getName(), registration );
		return registration;
	}

	@Override
	public void addListener( Class<? extends EventListener> listenerClass ) {
		listeners.add( listenerClass );
	}

	@Override
	public void addListener( String className ) {
		listeners.add( className );
	}

	@Override
	public <T extends EventListener> void addListener( T t ) {
		listeners.add( t );
	}

	/**
	 * @return list of all listeners that were registered in their raw type (class name, class or instance)
	 */
	public List<Object> getListeners() {
		return Collections.unmodifiableList( listeners );
	}
}
