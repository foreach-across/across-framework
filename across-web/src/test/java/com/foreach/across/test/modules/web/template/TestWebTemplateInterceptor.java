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
package com.foreach.across.test.modules.web.template;

import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.template.WebTemplateProcessor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
public class TestWebTemplateInterceptor
{
	@Mock
	private WebTemplateRegistry templateRegistry;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private WebTemplateInterceptor interceptor;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks( this );

		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

		interceptor = new WebTemplateInterceptor( templateRegistry );
	}

	@Test
	public void postHandleDoesNothingIfNoPartialOrTemplateProcessor() {
		ModelAndView mav = mock( ModelAndView.class );
		interceptor.postHandle( request, response, null, null );
		verifyNoMoreInteractions( templateRegistry, mav );
	}

	@Test
	public void postHandleShouldSendToTemplateProcessorIfPresentAsRequestAttribute() {
		WebTemplateProcessor template = mock( WebTemplateProcessor.class );
		request.setAttribute( WebTemplateInterceptor.PROCESSOR_ATTRIBUTE, template );

		Object handler = new Object();
		ModelAndView mav = mock( ModelAndView.class );
		interceptor.postHandle( request, response, handler, mav );

		verify( template ).applyTemplate( request, response, handler, mav );
	}

	@Test
	public void postHandleShouldSkipPartialIfTemplateProcessorIsPresent() {
		WebTemplateProcessor template = mock( WebTemplateProcessor.class );
		request.setAttribute( WebTemplateInterceptor.PROCESSOR_ATTRIBUTE, template );
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "myfragment" );

		Object handler = new Object();
		ModelAndView mav = mock( ModelAndView.class );
		interceptor.postHandle( request, response, handler, mav );

		verify( template ).applyTemplate( request, response, handler, mav );
		verifyNoMoreInteractions( mav );
	}

	@Test
	public void postHandleShouldAppendPartialFragmentToViewName() {
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "myfragment" );

		ModelAndView mav = new ModelAndView( "myview" );
		interceptor.postHandle( request, response, null, mav );

		assertEquals( "myview::myfragment", mav.getViewName() );
	}

	@Test
	public void postHandleShouldNotAppendPartialFragmentIfViewNameAlreadyHasFragment() {
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "myfragment" );

		ModelAndView mav = new ModelAndView( "myview :: otherfragment" );
		interceptor.postHandle( request, response, null, mav );

		assertEquals( "myview :: otherfragment", mav.getViewName() );
	}


	// AX-120 - NPE on partial view rendering
	@Test
	public void postHandleWithPartialShouldNotThrowErrorIfModelAndViewIsNull() {
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "myfragment" );

		interceptor.postHandle( request, response, null, null );
	}
}
