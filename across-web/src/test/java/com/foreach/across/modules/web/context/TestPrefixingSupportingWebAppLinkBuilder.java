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
package com.foreach.across.modules.web.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPrefixingSupportingWebAppLinkBuilder
{
	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private WebAppPathResolver pathResolver;

	@InjectMocks
	private PrefixingSupportingWebAppLinkBuilder linkBuilder;

	@Before
	public void setUp() throws Exception {
		contextPath( "/ctx" );
	}

	@Test
	public void nullLink() {
		when( response.encodeURL( "" ) ).thenReturn( "encoded" );

		assertEquals( "encoded", linkBuilder.buildLink( null ) );
		assertEquals( "", linkBuilder.buildLink( null, false ) );
		verify( pathResolver, never() ).path( anyString() );
	}

	@Test
	public void emptyLink() {
		when( response.encodeURL( "" ) ).thenReturn( "encoded" );
		when( pathResolver.path( "" ) ).thenReturn( "" );

		assertEquals( "encoded", linkBuilder.buildLink( "" ) );
		assertEquals( "", linkBuilder.buildLink( "", false ) );
	}

	@Test
	public void relativePathEncoded() {
		when( pathResolver.path( "/test-link" ) ).thenReturn( "/prefixed" );
		when( response.encodeURL( "/ctx/prefixed" ) ).thenReturn( "encoded" );

		assertEquals( "encoded", linkBuilder.buildLink( "/test-link" ) );
	}

	@Test
	public void relativePathWithoutEncode() {
		when( pathResolver.path( "/test-link" ) ).thenReturn( "/prefixed" );

		assertEquals( "/ctx/prefixed", linkBuilder.buildLink( "/test-link", false ) );
	}

	@Test
	public void noContextPath() {
		contextPath( "/" );
		when( pathResolver.path( "/test-link" ) ).thenReturn( "/prefixed" );
		when( response.encodeURL( "/prefixed" ) ).thenReturn( "encoded" );

		assertEquals( "encoded", linkBuilder.buildLink( "/test-link" ) );
	}

	@Test
	public void mailtoIsNeverEncoded() {
		when( pathResolver.path( "mailto:john@doe.com" ) ).thenReturn( "mailto:john@doe.com" );

		assertEquals( "mailto:john@doe.com", linkBuilder.buildLink( "mailto:john@doe.com" ) );
		verify( response, never() ).encodeURL( anyString() );
	}

	@Test
	public void hashIsNeverEncoded() {
		when( pathResolver.path( "#" ) ).thenReturn( "#" );

		assertEquals( "#", linkBuilder.buildLink( "#" ) );
		verify( response, never() ).encodeURL( anyString() );
	}

	@Test
	public void serverRelativePath() {
		when( pathResolver.path( "~/custom-path" ) ).thenReturn( "~/custom-path" );
		when( response.encodeURL( "/custom-path" ) ).thenReturn( "encoded" );

		assertEquals( "encoded", linkBuilder.buildLink( "~/custom-path" ) );
	}

	private void contextPath( String path ) {
		when( request.getContextPath() ).thenReturn( path );
		linkBuilder.setRequest( request );
	}
}
