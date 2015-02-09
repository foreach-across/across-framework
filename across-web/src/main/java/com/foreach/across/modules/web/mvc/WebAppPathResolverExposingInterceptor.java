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

import com.foreach.across.modules.web.context.WebAppPathResolver;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Puts the configured {@link com.foreach.across.modules.web.context.WebAppPathResolver} on the request context.
 * Views can use this resolver by default for all
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.context.PrefixingPathRegistry
 * @see com.foreach.across.modules.web.context.PrefixingPathContext
 */
public class WebAppPathResolverExposingInterceptor extends HandlerInterceptorAdapter
{
	private final WebAppPathResolver webAppPathResolver;

	public WebAppPathResolverExposingInterceptor( WebAppPathResolver webAppPathResolver ) {
		Assert.notNull( webAppPathResolver );
		this.webAppPathResolver = webAppPathResolver;
	}

	@Override
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler )
			throws ServletException, IOException {
		WebResourceUtils.storePathResolver( webAppPathResolver, request );
		return true;
	}
}
