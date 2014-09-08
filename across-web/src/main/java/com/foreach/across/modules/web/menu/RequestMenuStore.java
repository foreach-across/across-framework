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

package com.foreach.across.modules.web.menu;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores Menu instance as attribute on the request, under the name specified.
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestMenuStore implements MenuStore
{
	@Autowired
	private HttpServletRequest request;

	public void save( String name, Menu menu ) {
		request.setAttribute( StringUtils.defaultIfBlank( name, menu.getClass().getName() ), menu );
	}

	public Menu get( String name ) {
		return (Menu) request.getAttribute( name );
	}

	@SuppressWarnings("unchecked")
	public <T extends Menu> T get( String name, Class<T> menuType ) {
		return (T) request.getAttribute( StringUtils.defaultIfBlank( name, menuType.getName() ) );
	}
}
