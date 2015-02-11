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

package com.foreach.across.modules.web.mvc;

import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Replacement of {@link org.springframework.web.servlet.config.annotation.InterceptorRegistry} that supports
 * adding an interceptor to the front of the list, keeping existing support for
 * {@link org.springframework.web.servlet.handler.MappedInterceptor} intact.
 */
@SuppressWarnings("all")
public class InterceptorRegistry extends org.springframework.web.servlet.config.annotation.InterceptorRegistry
{
	private final LinkedList<ExtendedInterceptorRegistration> registrations = new LinkedList<>();

	public InterceptorRegistration addFirst( HandlerInterceptor interceptor ) {
		ExtendedInterceptorRegistration registration = new ExtendedInterceptorRegistration( interceptor );
		registrations.addFirst( registration );
		return registration;
	}

	public InterceptorRegistration addFirst( WebRequestInterceptor interceptor ) {
		WebRequestHandlerInterceptorAdapter adapted = new WebRequestHandlerInterceptorAdapter( interceptor );
		ExtendedInterceptorRegistration registration = new ExtendedInterceptorRegistration( adapted );
		registrations.addFirst( registration );
		return registration;
	}

	@Override
	public InterceptorRegistration addInterceptor( HandlerInterceptor interceptor ) {
		ExtendedInterceptorRegistration registration = new ExtendedInterceptorRegistration( interceptor );
		registrations.add( registration );
		return registration;
	}

	@Override
	public InterceptorRegistration addWebRequestInterceptor( WebRequestInterceptor interceptor ) {
		WebRequestHandlerInterceptorAdapter adapted = new WebRequestHandlerInterceptorAdapter( interceptor );
		ExtendedInterceptorRegistration registration = new ExtendedInterceptorRegistration( adapted );
		registrations.add( registration );
		return registration;
	}

	@Override
	public List<Object> getInterceptors() {
		List<Object> interceptors = new ArrayList<Object>();

		for ( ExtendedInterceptorRegistration registration : registrations ) {
			interceptors.add( registration.getInterceptor() );
		}

		return interceptors;
	}

	public static class ExtendedInterceptorRegistration extends InterceptorRegistration
	{
		public ExtendedInterceptorRegistration( HandlerInterceptor interceptor ) {
			super( interceptor );
		}

		@Override
		protected Object getInterceptor() {
			return super.getInterceptor();
		}
	}
}
