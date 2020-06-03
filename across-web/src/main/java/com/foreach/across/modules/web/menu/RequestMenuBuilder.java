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

package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.events.BuildMenuEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Builds a menu and assigns the request path to the selected path.
 */
@Service
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestMenuBuilder<T extends Menu, E extends BuildMenuEvent<T>> implements MenuBuilder<T, E>
{
	@Autowired
	private HttpServletRequest request;

	@SuppressWarnings("unchecked")
	public T build() {
		Menu menu = new Menu();

		setContext( menu );
		return (T) menu;
	}

	@SuppressWarnings( "unchecked" )
	protected E createEvent( T menu ) {
		return (E) new BuildMenuEvent<T>( menu );
	}

	public E buildEvent( T menu ) {
		E e = createEvent( menu );
		e.setMenuSelector( MenuSelector.byHttpServletRequest( request ) );
		return e;
	}

	protected void setContext( Menu menu ) {
		menu.setPath( request.getContextPath() );
	}
}
