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
package com.foreach.across.modules.web.resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestWebResourceUtils
{
	@Before
	@After
	public void reset() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void currentWebResourceRegistry() {
		assertEquals( Optional.empty(), WebResourceUtils.currentRegistry() );

		WebResourceRegistry registry = mock( WebResourceRegistry.class );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( registry );
		RequestContextHolder.setRequestAttributes( attributes );

		assertEquals( Optional.of( registry ), WebResourceUtils.currentRegistry() );
	}

	@Test
	@SuppressWarnings("all")
	public void getWebResourceRegistry() {
		Optional<WebResourceRegistry> expected = Optional.of( mock( WebResourceRegistry.class ) );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( expected.get() );
		assertEquals( expected, WebResourceUtils.getRegistry( attributes ) );

		WebRequest webRequest = mock( WebRequest.class );
		when( webRequest.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( expected.get() );
		assertEquals( expected, WebResourceUtils.getRegistry( webRequest ) );

		HttpServletRequest servletRequest = mock( HttpServletRequest.class );
		when( servletRequest.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY ) ).thenReturn( expected.get() );
		assertEquals( expected, WebResourceUtils.getRegistry( servletRequest ) );
	}
}
