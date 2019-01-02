/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.modules.web.template;

import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebTemplateInterceptor
{
	@Mock
	private WebTemplateRegistry templateRegistry;

	@Mock
	private PrefixingPathRegistry prefixingPathRegistry;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private WebTemplateInterceptor interceptor;

	@Before
	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

		interceptor = new WebTemplateInterceptor( templateRegistry );
		interceptor.setPrefixingPathRegistry( prefixingPathRegistry );
	}

	@Test
	public void preHandleShouldSetPartialParameters() {
		interceptor.preHandle( request, response, null );
		assertNull( request.getAttribute( WebTemplateInterceptor.PARTIAL_PARAMETER ) );
		assertNull( request.getAttribute( WebTemplateInterceptor.RENDER_FRAGMENT ) );
		assertNull( request.getAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT ) );

		request = new MockHttpServletRequest();
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "myfrag" );
		interceptor.preHandle( request, response, null );
		assertEquals( "myfrag", request.getAttribute( WebTemplateInterceptor.PARTIAL_PARAMETER ) );
		assertEquals( "myfrag", request.getAttribute( WebTemplateInterceptor.RENDER_FRAGMENT ) );
		assertNull( request.getAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT ) );

		request = new MockHttpServletRequest();
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "::myElement" );
		interceptor.preHandle( request, response, null );
		assertEquals( "::myElement", request.getAttribute( WebTemplateInterceptor.PARTIAL_PARAMETER ) );
		assertEquals( "myElement", request.getAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT ) );
		assertNull( request.getAttribute( WebTemplateInterceptor.RENDER_FRAGMENT ) );

		request = new MockHttpServletRequest();
		request.setParameter( WebTemplateInterceptor.PARTIAL_PARAMETER, "myfrag::myElement" );
		interceptor.preHandle( request, response, null );
		assertEquals( "myfrag::myElement", request.getAttribute( WebTemplateInterceptor.PARTIAL_PARAMETER ) );
		assertEquals( "myfrag", request.getAttribute( WebTemplateInterceptor.RENDER_FRAGMENT ) );
		assertEquals( "myElement", request.getAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT ) );
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
		request.setAttribute( WebTemplateInterceptor.RENDER_FRAGMENT, "myfragment" );

		Object handler = new Object();
		ModelAndView mav = mock( ModelAndView.class );
		interceptor.postHandle( request, response, handler, mav );

		verify( template ).applyTemplate( request, response, handler, mav );
	}

	@Test
	public void postHandleShouldAppendPartialFragmentToViewName() {
		request.setAttribute( WebTemplateInterceptor.RENDER_FRAGMENT, "myfragment" );

		ModelAndView mav = new ModelAndView( "myview" );
		interceptor.postHandle( request, response, null, mav );

		assertEquals( "myview::myfragment", mav.getViewName() );
	}

	@Test
	public void postHandleShouldNotAppendPartialFragmentIfViewNameAlreadyHasFragment() {
		request.setAttribute( WebTemplateInterceptor.RENDER_FRAGMENT, "myfragment" );

		ModelAndView mav = new ModelAndView( "myview :: otherfragment" );
		interceptor.postHandle( request, response, null, mav );

		assertEquals( "myview :: otherfragment", mav.getViewName() );
	}

	@Test
	public void postHandleShouldNotAppendPartialFragmentToForward() {
		request.setAttribute( WebTemplateInterceptor.RENDER_FRAGMENT, "myfragment" );

		when( prefixingPathRegistry.path( "forward:/bar" ) ).thenReturn( "forward:/prefixed/bar" );

		ModelAndView mav = new ModelAndView( "forward:/bar" );
		interceptor.postHandle( request, response, null, mav );

		assertEquals( "forward:/prefixed/bar", mav.getViewName() );
	}

	@Test
	public void postHandleShouldNotAppendPartialFragmentToRedirect() {
		request.setAttribute( WebTemplateInterceptor.RENDER_FRAGMENT, "myfragment" );

		when( prefixingPathRegistry.path( "redirect:/foo" ) ).thenReturn( "redirect:/prefixed/foo" );

		ModelAndView mav = new ModelAndView( "redirect:/foo" );
		interceptor.postHandle( request, response, null, mav );

		assertEquals( "redirect:/prefixed/foo", mav.getViewName() );
	}

	// AX-120 - NPE on partial view rendering
	@Test
	public void postHandleWithPartialShouldNotThrowErrorIfModelAndViewIsNull() {
		request.setAttribute( WebTemplateInterceptor.RENDER_FRAGMENT, "myfragment" );

		interceptor.postHandle( request, response, null, null );
	}
}
