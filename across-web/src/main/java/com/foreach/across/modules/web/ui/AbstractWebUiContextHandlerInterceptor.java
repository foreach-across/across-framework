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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor that constructs the WebUiContext corresponding to this particular request.
 * Requires an AbstractWebUiContextRegistry to be present in the ApplicationContext.
 *
 * @param <T> Specific WebUiContext implementation (use interface).
 */
@Deprecated
public abstract class AbstractWebUiContextHandlerInterceptor<T extends WebUiContext> extends HandlerInterceptorAdapter
{
	@Autowired
	private BeanFactory beanFactory;

	@Autowired(required = false)
	private MessageSource messageSource;

	@SuppressWarnings({ "unchecked", "SignatureDeclareThrowsException" })

	@Override
	public boolean preHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		AbstractWebUiContextRegistry registry = beanFactory.getBean( AbstractWebUiContextRegistry.class );

		WebUiContext webUiContext = createWebUiContext( request, response, messageSource );
		registry.setWebUiContext( webUiContext );

		return super.preHandle( request, response, handler );
	}

	protected abstract T createWebUiContext(
			HttpServletRequest request, HttpServletResponse response, MessageSource messageSource );
}
