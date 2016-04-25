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

import com.foreach.across.modules.web.context.WebAppPathResolver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;

public class WebResourceUtils
{
	/**
	 * Default key under which the registry is put in the request attributes
	 */
	public static final String REGISTRY_ATTRIBUTE_KEY = "webResourceRegistry";

	public static final String PATH_RESOLVER_ATTRIBUTE_KEY = WebAppPathResolver.class.getName();

	protected WebResourceUtils() {
	}

	public static void storeRegistry( WebResourceRegistry registry, HttpServletRequest request ) {
		request.setAttribute( REGISTRY_ATTRIBUTE_KEY, registry );
	}

	/**
	 * @return the {@link WebResourceRegistry} for the request bound to the current thread
	 */
	public static WebResourceRegistry currentRegistry() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? getRegistry( requestAttributes ) : null;
	}

	public static WebResourceRegistry getRegistry( RequestAttributes request ) {
		return (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY, WebRequest.SCOPE_REQUEST );
	}

	public static WebResourceRegistry getRegistry( WebRequest request ) {
		return getRegistry( (RequestAttributes) request );
	}

	public static WebResourceRegistry getRegistry( HttpServletRequest request ) {
		return (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY );
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
}
