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

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.servlet.MultipartConfigFactory;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletSecurityElement;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestMockServletRegistration
{
	private MockAcrossServletContext servletContext;

	@Before
	public void resetServletContext() {
		servletContext = new MockAcrossServletContext();
	}

	@Test
	public void createWithInstance() {
		Servlet servlet = mock( Servlet.class );

		MockServletRegistration registration = new MockServletRegistration( servletContext, "someName", servlet );
		assertEquals( "someName", registration.getName() );
		assertEquals( "someName", registration.getServletName() );
		assertSame( servletContext, registration.getServletContext() );
		assertSame( servlet, registration.getServlet() );
		assertEquals( servlet.getClass(), registration.getServletClass() );
		assertEquals( servlet.getClass().getName(), registration.getClassName() );
		assertDefaults( registration );
	}

	@Test
	public void createWithClass() {
		MockServletRegistration registration = new MockServletRegistration( servletContext, "someName", Servlet.class );
		assertEquals( "someName", registration.getName() );
		assertEquals( "someName", registration.getServletName() );
		assertSame( servletContext, registration.getServletContext() );
		assertNull( registration.getServlet() );
		assertEquals( Servlet.class, registration.getServletClass() );
		assertEquals( Servlet.class.getName(), registration.getClassName() );
		assertDefaults( registration );
	}

	@Test
	public void createWithClassName() {
		MockServletRegistration registration = new MockServletRegistration( servletContext, "someName",
		                                                                    Servlet.class.getName() );
		assertEquals( "someName", registration.getName() );
		assertEquals( "someName", registration.getServletName() );
		assertSame( servletContext, registration.getServletContext() );
		assertNull( registration.getServlet() );
		assertNull( registration.getServletClass() );
		assertEquals( Servlet.class.getName(), registration.getClassName() );
		assertDefaults( registration );
	}

	private void assertDefaults( MockServletRegistration registration ) {
		assertFalse( registration.isAsyncSupported() );
		assertNull( registration.getMultipartConfig() );
		assertNull( registration.getRunAsRole() );
		assertTrue( registration.getMappings().isEmpty() );
		assertNull( registration.getServletSecurity() );
		assertEquals( 0, registration.getLoadOnStartup() );
	}

	@Test
	public void initParameters() {
		MockServletRegistration registration = new MockServletRegistration( servletContext, "name", Servlet.class );

		assertTrue( registration.getInitParameters().isEmpty() );
		assertTrue( registration.setInitParameter( "one", "two" ) );
		assertFalse( registration.setInitParameter( "one", "three" ) );

		assertEquals( "two", registration.getInitParameter( "one" ) );

		Map<String, String> expected = new LinkedHashMap<>();
		expected.put( "two", "x" );
		expected.put( "one", "five" );
		expected.put( "value", "other" );

		assertEquals( Collections.singleton( "one" ), registration.setInitParameters( expected ) );

		expected.put( "one", "two" );

		assertEquals( expected, registration.getInitParameters() );

		Enumeration<String> parameterNames = registration.getInitParameterNames();
		assertEquals( "one", parameterNames.nextElement() );
		assertEquals( "two", parameterNames.nextElement() );
		assertEquals( "value", parameterNames.nextElement() );
	}

	@Test
	public void loadOnStartup() {
		MockServletRegistration registration = new MockServletRegistration( servletContext, "name", Servlet.class );
		assertEquals( 0, registration.getLoadOnStartup() );
		registration.setLoadOnStartup( 1 );
		assertEquals( 1, registration.getLoadOnStartup() );
	}

	@Test
	public void runAsRole() {
		MockServletRegistration registration = new MockServletRegistration( servletContext, "name", Servlet.class );
		assertNull( registration.getRunAsRole() );
		registration.setRunAsRole( "some role" );
		assertEquals( "some role", registration.getRunAsRole() );
	}

	@Test
	public void multipartConfig() {
		MultipartConfigElement multipartConfigElement = new MultipartConfigFactory().createMultipartConfig();
		MockServletRegistration registration = new MockServletRegistration( servletContext, "name", Servlet.class );
		assertNull( registration.getMultipartConfig() );
		registration.setMultipartConfig( multipartConfigElement );
		assertSame( multipartConfigElement, registration.getMultipartConfig() );
	}

	@Test
	public void servletSecurity() {
		ServletSecurityElement servletSecurityElement = new ServletSecurityElement();
		MockServletRegistration registration = new MockServletRegistration( servletContext, "name", Servlet.class );
		assertNull( registration.getServletSecurity() );
		registration.setServletSecurity( servletSecurityElement );
		assertSame( servletSecurityElement, registration.getServletSecurity() );
	}

	@Test
	public void urlMappings() {
		MockServletRegistration registration = new MockServletRegistration( servletContext, "name", Servlet.class );
		assertTrue( registration.getMappings().isEmpty() );

		registration.addMapping( "one", "two" );
		registration.addMapping( "three" );

		assertArrayEquals(
				new String[] { "one", "two", "three" },
				registration.getMappings().toArray( new String[3] )
		);
	}
}
