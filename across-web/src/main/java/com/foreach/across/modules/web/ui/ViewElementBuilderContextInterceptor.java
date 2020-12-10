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

import com.foreach.across.modules.web.context.WebAppLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.support.LocalizedTextResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Registers a global {@link ViewElementBuilderContext} on the request and attaches it as the
 * current thread-local builder context.
 * <p/>
 * Extend this interceptor if you want to register additional default attributes.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class ViewElementBuilderContextInterceptor implements HandlerInterceptor
{
	private WebAppLinkBuilder webAppLinkBuilder;
	private MessageSource messageSource;
	private LocalizedTextResolver localizedTextResolver;

	@Override
	public final boolean preHandle( HttpServletRequest request,
	                                HttpServletResponse response,
	                                Object handler ) throws Exception {
		WebResourceUtils.storeMessageSource( messageSource, request );
		WebResourceUtils.storeLinkBuilder( webAppLinkBuilder, request );

		ViewElementBuilderContext builderContext = createDefaultViewElementBuilderContext( request );
		WebResourceUtils.storeViewElementBuilderContext( builderContext, request );
		ViewElementBuilderContextHolder.setViewElementBuilderContext( builderContext );

		return true;
	}

	protected ViewElementBuilderContext createDefaultViewElementBuilderContext( HttpServletRequest request ) {
		DefaultViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		builderContext.setWebAppLinkBuilder( webAppLinkBuilder );
		builderContext.setMessageSource( messageSource );
		builderContext.setLocalizedTextResolver( localizedTextResolver );
		return builderContext;
	}

	@Override
	public final void afterCompletion( HttpServletRequest request,
	                                   HttpServletResponse response,
	                                   Object handler,
	                                   Exception ex ) throws Exception {
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}

	@Autowired
	protected void setWebAppLinkBuilder( WebAppLinkBuilder webAppLinkBuilder ) {
		this.webAppLinkBuilder = webAppLinkBuilder;
	}

	@Autowired
	protected void setMessageSource( MessageSource messageSource ) {
		this.messageSource = messageSource;
	}

	@Autowired
	protected void setLocalizedTextResolver( LocalizedTextResolver localizedTextResolver ) {
		this.localizedTextResolver = localizedTextResolver;
	}
}
