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

import com.foreach.across.modules.web.context.WebAppLinkBuilder;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Utilities for accessing the {@link WebResourceRegistry} and the  {@link WebAppPathResolver} when
 * running in a web context.
 *
 * @author Arne Vandamme
 * @since 1.0.0
 */
public class WebResourceUtils
{
	/**
	 * Attribute key under which the registry is put in the request attributes
	 */
	public static final String REGISTRY_ATTRIBUTE_KEY = "webResourceRegistry";

	public static final String PATH_RESOLVER_ATTRIBUTE_KEY = WebAppPathResolver.class.getName();
	public static final String LINK_BUILDER_ATTRIBUTE_KEY = WebAppLinkBuilder.class.getName();
	public static final String VIEW_ELEMENT_BUILDER_CONTEXT_KEY = ViewElementBuilderContext.class.getName();

	protected WebResourceUtils() {
	}

	public static void storeRegistry( WebResourceRegistry registry, HttpServletRequest request ) {
		request.setAttribute( REGISTRY_ATTRIBUTE_KEY, registry );
	}

	/**
	 * @return the {@link WebResourceRegistry} for the request bound to the current thread
	 */
	public static Optional<WebResourceRegistry> currentRegistry() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? getRegistry( requestAttributes ) : Optional.empty();
	}

	public static Optional<WebResourceRegistry> getRegistry( RequestAttributes request ) {
		return Optional.ofNullable(
				(WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY, WebRequest.SCOPE_REQUEST )
		);
	}

	public static Optional<WebResourceRegistry> getRegistry( WebRequest request ) {
		return getRegistry( (RequestAttributes) request );
	}

	public static Optional<WebResourceRegistry> getRegistry( HttpServletRequest request ) {
		return Optional.ofNullable( (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY ) );
	}

	public static void storePathResolver( WebAppPathResolver pathResolver, HttpServletRequest request ) {
		request.setAttribute( PATH_RESOLVER_ATTRIBUTE_KEY, pathResolver );
	}

	/**
	 * @return the {@link WebAppPathResolver} for the request bound to the current thread
	 */
	public static WebAppPathResolver currentPathResolver() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? getPathResolver( requestAttributes ) : null;
	}

	public static WebAppPathResolver getPathResolver( RequestAttributes request ) {
		return (WebAppPathResolver) request.getAttribute( PATH_RESOLVER_ATTRIBUTE_KEY, WebRequest.SCOPE_REQUEST );
	}

	public static WebAppPathResolver getPathResolver( HttpServletRequest request ) {
		return (WebAppPathResolver) request.getAttribute( PATH_RESOLVER_ATTRIBUTE_KEY );
	}

	public static void storeLinkBuilder( WebAppLinkBuilder pathResolver, HttpServletRequest request ) {
		request.setAttribute( LINK_BUILDER_ATTRIBUTE_KEY, pathResolver );
	}

	/**
	 * @return the {@link WebAppLinkBuilder} for the request bound to the current thread
	 */
	public static Optional<WebAppLinkBuilder> currentLinkBuilder() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? getLinkBuilder( requestAttributes ) : Optional.empty();
	}

	public static Optional<WebAppLinkBuilder> getLinkBuilder( RequestAttributes request ) {
		return Optional.ofNullable( (WebAppLinkBuilder) request.getAttribute( LINK_BUILDER_ATTRIBUTE_KEY,
		                                                                      WebRequest.SCOPE_REQUEST ) );
	}

	public static Optional<WebAppLinkBuilder> getLinkBuilder( HttpServletRequest request ) {
		return Optional.ofNullable( (WebAppLinkBuilder) request.getAttribute( LINK_BUILDER_ATTRIBUTE_KEY ) );
	}

	public static void storeViewElementBuilderContext( ViewElementBuilderContext builderContext,
	                                                   HttpServletRequest request ) {
		request.setAttribute( VIEW_ELEMENT_BUILDER_CONTEXT_KEY, builderContext );
	}

	/**
	 * @return the {@link ViewElementBuilderContext} for the request bound to the current thread
	 */
	public static Optional<ViewElementBuilderContext> currentViewElementBuilderContext() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? getViewElementBuilderContext( requestAttributes ) : Optional.empty();
	}

	public static Optional<ViewElementBuilderContext> getViewElementBuilderContext( RequestAttributes request ) {
		return Optional.ofNullable( (ViewElementBuilderContext) request.getAttribute( VIEW_ELEMENT_BUILDER_CONTEXT_KEY,
		                                                                              WebRequest.SCOPE_REQUEST ) );
	}

	public static Optional<ViewElementBuilderContext> getViewElementBuilderContext( HttpServletRequest request ) {
		return Optional.ofNullable(
				(ViewElementBuilderContext) request.getAttribute( VIEW_ELEMENT_BUILDER_CONTEXT_KEY ) );
	}
}
