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

import com.foreach.across.test.MockFilterRegistration.MappingRule;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestMockFilterRegistration
{
	@Test
	public void createWithInstance() {
		Filter filter = mock( Filter.class );

		MockFilterRegistration registration = new MockFilterRegistration( "someName", filter );
		assertEquals( "someName", registration.getName() );
		assertSame( filter, registration.getFilter() );
		assertEquals( filter.getClass(), registration.getFilterClass() );
		assertEquals( filter.getClass().getName(), registration.getClassName() );
		assertFalse( registration.isAsyncSupported() );
	}

	@Test
	public void createWithClass() {
		MockFilterRegistration registration = new MockFilterRegistration( "someName", Filter.class );
		assertEquals( "someName", registration.getName() );
		assertNull( registration.getFilter() );
		assertEquals( Filter.class, registration.getFilterClass() );
		assertEquals( Filter.class.getName(), registration.getClassName() );
		assertFalse( registration.isAsyncSupported() );
	}

	@Test
	public void createWithClassName() {
		MockFilterRegistration registration = new MockFilterRegistration( "someName", Filter.class.getName() );
		assertEquals( "someName", registration.getName() );
		assertNull( registration.getFilter() );
		assertNull( registration.getFilterClass() );
		assertEquals( Filter.class.getName(), registration.getClassName() );
		assertFalse( registration.isAsyncSupported() );
	}

	@Test
	public void initParameters() {
		MockFilterRegistration registration = new MockFilterRegistration( "name", Filter.class );

		assertTrue( registration.getInitParameters().isEmpty() );
		assertTrue( registration.setInitParameter( "one", "two" ) );
		assertFalse( registration.setInitParameter( "one", "three" ) );

		assertEquals( "two", registration.getInitParameter( "one" ) );

		Map<String, String> expected = new HashMap<>();
		expected.put( "two", "x" );
		expected.put( "one", "five" );
		expected.put( "value", "other" );

		assertEquals( Collections.singleton( "one" ), registration.setInitParameters( expected ) );

		expected.put( "one", "two" );

		assertEquals( expected, registration.getInitParameters() );
	}

	@Test
	public void mappingRules() {
		MockFilterRegistration registration = new MockFilterRegistration( "name", Filter.class );
		registration.addMappingForServletNames(
				EnumSet.allOf( DispatcherType.class ), true, "servletOne", "servletTwo"
		);
		registration.addMappingForUrlPatterns( null, false, "/test" );
		registration.addMappingForServletNames( EnumSet.of( DispatcherType.REQUEST ), false, "servletThree" );
		registration.addMappingForUrlPatterns(
				EnumSet.of( DispatcherType.ASYNC, DispatcherType.REQUEST ), true, "/one", "/two"
		);

		assertEquals(
				new MappingRule( false, true, EnumSet.allOf( DispatcherType.class ), "servletOne", "servletTwo" ),
				registration.getMappingRules().get( 0 )
		);
		assertEquals( new MappingRule( true, false, null, "/test" ), registration.getMappingRules().get( 1 ) );
		assertEquals(
				new MappingRule( false, false, EnumSet.of( DispatcherType.REQUEST ), "servletThree" ),
				registration.getMappingRules().get( 2 )
		);
		assertEquals(
				new MappingRule(
						true, true, EnumSet.of( DispatcherType.ASYNC, DispatcherType.REQUEST ), "/one", "/two"
				),
				registration.getMappingRules().get( 3 )
		);

		assertEquals(
				new HashSet<>( Arrays.asList( "servletOne", "servletTwo", "servletThree" ) ),
				registration.getServletNameMappings()
		);
		assertEquals(
				new HashSet<>( Arrays.asList( "/test", "/one", "/two" ) ),
				registration.getUrlPatternMappings()
		);
	}
}
