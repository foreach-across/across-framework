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
