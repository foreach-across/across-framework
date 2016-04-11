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
import com.foreach.across.modules.web.servlet.JspPropertyGroupDescriptorStub;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestMockServletContext
{
	@Test
	public void dynamicRegistration() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();
		assertFalse( servletContext.isInitialized() );
		assertEquals( true, servletContext.getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER ) );

		servletContext.initialize();
		assertTrue( servletContext.isInitialized() );
		assertNull( servletContext.getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER ) );

		servletContext = new MockAcrossServletContext( true );
		assertFalse( servletContext.isInitialized() );

		servletContext = new MockAcrossServletContext( false );
		assertTrue( servletContext.isInitialized() );
	}

	@Test
	public void createFilterWithInstance() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		Filter filter = mock( Filter.class );
		MockFilterRegistration registration = servletContext.addFilter( "one", filter );
		assertNotNull( registration );
		assertEquals( "one", registration.getName() );
		assertSame( filter, registration.getFilter() );

		assertSame( registration, servletContext.getFilterRegistration( "one" ) );
		assertSame( registration, servletContext.addFilter( "one", Filter.class.getName() ) );
	}

	@Test
	public void createFilterWithClass() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		MockFilterRegistration registration = servletContext.addFilter( "two", Filter.class );
		assertNotNull( registration );
		assertEquals( "two", registration.getName() );
		assertSame( Filter.class, registration.getFilterClass() );

		assertSame( registration, servletContext.getFilterRegistration( "two" ) );
		assertSame( registration, servletContext.addFilter( "two", mock( Filter.class ) ) );
	}

	@Test
	public void createFilterWithClassName() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		MockFilterRegistration registration = servletContext.addFilter( "three", Filter.class.getName() );
		assertNotNull( registration );
		assertEquals( "three", registration.getName() );
		assertSame( Filter.class.getName(), registration.getClassName() );

		assertSame( registration, servletContext.getFilterRegistration( "three" ) );
		assertSame( registration, servletContext.addFilter( "three", mock( Filter.class ) ) );
	}

	@Test
	public void filterRegistrationsAreKeptInOrder() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		MockFilterRegistration one = servletContext.addFilter( "one", Filter.class.getName() );
		MockFilterRegistration two = servletContext.addFilter( "two", mock( Filter.class ) );
		MockFilterRegistration three = servletContext.addFilter( "three", Filter.class );

		Map<String, MockFilterRegistration> registrations = servletContext.getFilterRegistrations();
		assertArrayEquals(
				new String[] { "one", "two", "three" },
				registrations.keySet().toArray()
		);
		assertArrayEquals(
				new MockFilterRegistration[] { one, two, three },
				registrations.values().toArray()
		);
	}

	@Test
	public void createServletWithInstance() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		Servlet servlet = mock( Servlet.class );
		MockServletRegistration registration = servletContext.addServlet( "one", servlet );
		assertNotNull( registration );
		assertEquals( "one", registration.getName() );
		assertSame( servlet, registration.getServlet() );

		assertSame( registration, servletContext.getServletRegistration( "one" ) );
		assertSame( registration, servletContext.addServlet( "one", Servlet.class.getName() ) );
	}

	@Test
	public void createServletWithClass() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		MockServletRegistration registration = servletContext.addServlet( "two", Servlet.class );
		assertNotNull( registration );
		assertEquals( "two", registration.getName() );
		assertSame( Servlet.class, registration.getServletClass() );

		assertSame( registration, servletContext.getServletRegistration( "two" ) );
		assertSame( registration, servletContext.addServlet( "two", mock( Servlet.class ) ) );
	}

	@Test
	public void createServletWithClassName() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		MockServletRegistration registration = servletContext.addServlet( "three", Servlet.class.getName() );
		assertNotNull( registration );
		assertEquals( "three", registration.getName() );
		assertSame( Servlet.class.getName(), registration.getClassName() );

		assertSame( registration, servletContext.getServletRegistration( "three" ) );
		assertSame( registration, servletContext.addServlet( "three", mock( Servlet.class ) ) );
	}

	@Test
	public void servletRegistrationsAreKeptInOrder() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();

		MockServletRegistration one = servletContext.addServlet( "one", Servlet.class.getName() );
		MockServletRegistration two = servletContext.addServlet( "two", mock( Servlet.class ) );
		MockServletRegistration three = servletContext.addServlet( "three", Servlet.class );

		Map<String, MockServletRegistration> registrations = servletContext.getServletRegistrations();
		assertArrayEquals(
				new String[] { "one", "two", "three" },
				registrations.keySet().toArray()
		);
		assertArrayEquals(
				new MockServletRegistration[] { one, two, three },
				registrations.values().toArray()
		);
	}

	@Test
	public void listenersAreKeptInOrder() {
		EventListener instance = mock( EventListener.class );

		MockAcrossServletContext servletContext = new MockAcrossServletContext();
		servletContext.addListener( "someClassName" );
		servletContext.addListener( EventListener.class );
		servletContext.addListener( instance );

		assertEquals(
				Arrays.asList( "someClassName", EventListener.class, instance ),
				servletContext.getListeners()
		);
	}

	@Test
	public void filtersAndServletsAreInitializedOnce() throws ServletException {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();
		Filter filter = mock( Filter.class );
		Servlet servlet = mock( Servlet.class );

		MockFilterRegistration filterRegistration = servletContext.addFilter( "myFilter", filter );
		MockServletRegistration servletRegistration = servletContext.addServlet( "myServlet", servlet );

		servletContext.initialize();
		verify( filter ).init( filterRegistration );
		verify( servlet ).init( servletRegistration );

		// Call initialize again, still only one init call should be made
		servletContext.initialize();
		verify( filter ).init( filterRegistration );
		verify( servlet ).init( servletRegistration );
	}

	@Test
	public void illegalStateExceptionOnRegistrationActionsIfContextIsInitialized() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext( false );

		illegal( () -> servletContext.addFilter( "myFilter", mock( Filter.class ) ) );
		illegal( () -> servletContext.addFilter( "myFilter", Filter.class.getName() ) );
		illegal( () -> servletContext.addFilter( "myFilter", Filter.class ) );
		illegal( () -> servletContext.addServlet( "myServlet", mock( Servlet.class ) ) );
		illegal( () -> servletContext.addServlet( "myServlet", Servlet.class.getName() ) );
		illegal( () -> servletContext.addServlet( "myServlet", Servlet.class ) );
		illegal( () -> servletContext.addListener( mock( EventListener.class ) ) );
		illegal( () -> servletContext.addListener( EventListener.class ) );
		illegal( () -> servletContext.addListener( EventListener.class.getName() ) );
	}

	@Test
	public void jspConfigDescriptorIsCustomizable() {
		MockAcrossServletContext servletContext = new MockAcrossServletContext();
		JspConfigDescriptor jspConfigDescriptor = servletContext.getJspConfigDescriptor();
		JspPropertyGroupDescriptor propertyGroupDescriptor = new JspPropertyGroupDescriptorStub()
		{
			@Override
			public Collection<String> getUrlPatterns() {
				return Collections.singletonList( "*.jsp" );
			}

			@Override
			public String getScriptingInvalid() {
				return "true";
			}

		};
		jspConfigDescriptor.getJspPropertyGroups().add( propertyGroupDescriptor );
		assertEquals( 1, jspConfigDescriptor.getJspPropertyGroups().size() );
		assertEquals( 0, jspConfigDescriptor.getTaglibs().size() );
	}

	private void illegal( Runnable runnable ) {
		boolean exceptionThrown = false;
		try {
			runnable.run();
		}
		catch ( IllegalStateException ignore ) {
			exceptionThrown = true;
		}
		assertTrue( "IllegalStateException was expected but not thrown", exceptionThrown );
	}
}
