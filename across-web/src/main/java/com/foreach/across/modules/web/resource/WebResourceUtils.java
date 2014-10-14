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

import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;

public class WebResourceUtils
{
	/**
	 * Default key under which the registry is put in the request attributes
	 */
	public static final String REGISTRY_ATTRIBUTE_KEY = "webResourceRegistry";

	protected WebResourceUtils() {
	}

	public static void storeRegistry( WebResourceRegistry registry, HttpServletRequest request ) {
		request.setAttribute( REGISTRY_ATTRIBUTE_KEY, registry );
	}

	public static WebResourceRegistry getRegistry( WebRequest request ) {
		return (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY, WebRequest.SCOPE_REQUEST );
	}

	public static WebResourceRegistry getRegistry( HttpServletRequest request ) {
		return (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY );
	}
}
