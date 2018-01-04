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
package com.foreach.across.modules.web.ui;

import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGlobalContextSupportingViewElementBuilder
{
	@Mock
	private ViewElementBuilderContext threadLocal;

	@Mock
	private ViewElementBuilderContext requestBound;

	@Spy
	private GlobalContextSupportingViewElementBuilder builder;

	@Test(expected = IllegalStateException.class)
	public void exceptionIfNoGlobalContextAvailable() {
		builder.build();
	}

	@Test
	public void requestBoundIsUsedIfNoThreadLocal() {
		bindBuilderContextToRequest();

		builder.build();

		verify( builder ).build( requestBound );
		verify( builder, never() ).build( threadLocal );
	}

	private void bindBuilderContextToRequest() {
		assertEquals( Optional.empty(), WebResourceUtils.currentViewElementBuilderContext() );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.VIEW_ELEMENT_BUILDER_CONTEXT_KEY,
		                               RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( requestBound );
		RequestContextHolder.setRequestAttributes( attributes );
		assertEquals( Optional.of( requestBound ), WebResourceUtils.currentViewElementBuilderContext() );
	}

	@Test
	public void threadLocalTakesPrecedenceOverRequestBound() {
		bindBuilderContextToRequest();
		ViewElementBuilderContextHolder.setViewElementBuilderContext( threadLocal );

		builder.build();

		verify( builder ).build( threadLocal );
		verify( builder, never() ).build( requestBound );
	}

	@Before
	@After
	public void reset() {
		RequestContextHolder.resetRequestAttributes();
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}
}
