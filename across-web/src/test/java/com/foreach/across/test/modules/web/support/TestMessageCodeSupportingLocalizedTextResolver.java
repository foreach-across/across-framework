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
package com.foreach.across.test.modules.web.support;

import com.foreach.across.modules.web.support.MessageCodeSupportingLocalizedTextResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestMessageCodeSupportingLocalizedTextResolver
{
	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private MessageCodeSupportingLocalizedTextResolver textResolver;

	@Test
	public void actualTextResolving() {
		assertEquals( "my text", textResolver.resolveText( "my text" ) );
		assertEquals( null, textResolver.resolveText( null ) );
		assertEquals( "", textResolver.resolveText( "", "empty" ) );
		assertEquals( "empty", textResolver.resolveText( null, "empty" ) );
		assertEquals( "#{", textResolver.resolveText( "#{" ) );
		verifyNoMoreInteractions( messageSource );
	}

	@Test
	public void messageCodeWithoutDefaultValue() {
		when( messageSource.getMessage( "my.code", new Object[0], "my.code", Locale.US ) ).thenReturn( "resolved" );
		when( messageSource.getMessage( "my.other", new Object[0], "default", Locale.US ) ).thenReturn( "default" );

		assertEquals( "resolved", textResolver.resolveText( "#{my.code}", Locale.US ) );
		assertEquals( "default", textResolver.resolveText( "#{my.other}", "default", Locale.US ) );
		assertNull( textResolver.resolveText( "#{}" ) );
	}

	@Test
	public void messageCodeWithDefaultValue() {
		when( messageSource.getMessage( "my.code", new Object[0], "default", Locale.US ) ).thenReturn( "default" );
		when( messageSource.getMessage( "my.code", new Object[0], "", Locale.US ) ).thenReturn( "" );

		assertEquals( "default", textResolver.resolveText( "#{my.code=default}", Locale.US ) );
		assertEquals( "", textResolver.resolveText( "#{my.code=}", Locale.US ) );
	}

	@Test
	public void customDefaultValueTakesPrecedenceOverTokenBased() {
		when( messageSource.getMessage( "my.code", new Object[0], "custom-default", Locale.US ) ).thenReturn( "custom-default" );
		assertEquals( "custom-default", textResolver.resolveText( "#{my.code=default}", "custom-default", Locale.US ) );

	}
}
