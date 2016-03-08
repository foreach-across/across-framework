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

import javax.servlet.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mock version of a {@link javax.servlet.ServletRegistration.Dynamic} that does nothing but keep a number
 * of configured properties.  Support is limited and only intended in combination with {@link MockAcrossServletContext}.
 *
 * @author Marc Vanbrabant, Arne Vandamme
 */
public class MockServletRegistration extends AbstractMockRegistration implements ServletRegistration.Dynamic, ServletConfig
{
	private final Servlet servlet;
	private final Class<? extends Servlet> servletClass;

	private final Set<String> mappings = new LinkedHashSet<>();

	private int loadOnStartup;
	private String runAsRole;
	private ServletSecurityElement servletSecurity;
	private MultipartConfigElement multipartConfig;

	MockServletRegistration( MockAcrossServletContext servletContext, String name, Servlet servlet ) {
		super( servletContext, name, servlet.getClass().getName() );

		this.servlet = servlet;
		this.servletClass = servlet.getClass();
	}

	MockServletRegistration( MockAcrossServletContext servletContext,
	                         String name,
	                         Class<? extends Servlet> servletClass ) {
		super( servletContext, name, servletClass.getName() );

		this.servletClass = servletClass;
		this.servlet = null;
	}

	MockServletRegistration( MockAcrossServletContext servletContext, String name, String className ) {
		super( servletContext, name, className );

		this.servlet = null;
		this.servletClass = null;
	}

	/**
	 * @return servlet name
	 */
	@Override
	public String getServletName() {
		return getName();
	}

	/**
	 * @return filter instance if configured
	 */
	public Servlet getServlet() {
		return servlet;
	}

	/**
	 * @return filter class if configured
	 */
	public Class<? extends Servlet> getServletClass() {
		return servletClass;
	}

	@Override
	public void setLoadOnStartup( int loadOnStartup ) {
		this.loadOnStartup = loadOnStartup;
	}

	@Override
	public Set<String> setServletSecurity( ServletSecurityElement servletSecurity ) {
		this.servletSecurity = servletSecurity;
		return Collections.emptySet();
	}

	@Override
	public void setMultipartConfig( MultipartConfigElement multipartConfig ) {
		this.multipartConfig = multipartConfig;
	}

	@Override
	public void setRunAsRole( String roleName ) {
		this.runAsRole = roleName;
	}

	@Override
	public Set<String> addMapping( String... urlPatterns ) {
		Collections.addAll( mappings, urlPatterns );
		return Collections.emptySet();
	}

	@Override
	public Collection<String> getMappings() {
		return Collections.unmodifiableSet( mappings );
	}

	@Override
	public String getRunAsRole() {
		return runAsRole;
	}

	public int getLoadOnStartup() {
		return loadOnStartup;
	}

	public ServletSecurityElement getServletSecurity() {
		return servletSecurity;
	}

	public MultipartConfigElement getMultipartConfig() {
		return multipartConfig;
	}
}
