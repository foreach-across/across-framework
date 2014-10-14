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

import org.springframework.mock.web.MockServletContext;

import javax.servlet.*;
import java.util.Collection;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

public class MockAcrossServletContext extends MockServletContext
{
	@Override
	public ServletRegistration.Dynamic addServlet( String servletName, Class<? extends Servlet> servletClass ) {
		return new MockServletRegistration();
	}

	@Override
	public ServletRegistration.Dynamic addServlet( String servletName, String className ) {
		return new MockServletRegistration();
	}

	@Override
	public ServletRegistration.Dynamic addServlet( String servletName, Servlet servlet ) {
		return new MockServletRegistration();
	}

	@Override
	public FilterRegistration.Dynamic addFilter( String filterName, String className ) {
		return new MockFilterRegistration();
	}

	@Override
	public FilterRegistration.Dynamic addFilter( String filterName, Filter filter ) {
		return new MockFilterRegistration();
	}

	@Override
	public FilterRegistration.Dynamic addFilter( String filterName, Class<? extends Filter> filterClass ) {
		return new MockFilterRegistration();
	}

	@Override
	public void addListener( Class<? extends EventListener> listenerClass ) {
	}

	@Override
	public void addListener( String className ) {
	}

	@Override
	public <T extends EventListener> void addListener( T t ) {
	}
}
