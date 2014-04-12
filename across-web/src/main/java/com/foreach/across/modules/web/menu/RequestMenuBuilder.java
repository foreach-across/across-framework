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
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
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

	protected E createEvent( T menu ) {
		return (E)new BuildMenuEvent<T>( menu );
	}

	public E buildEvent( T menu ) {
		E e = createEvent( menu );
		e.setSelector( new RequestMenuSelector( request ) );
		return e;
	}

	protected void setContext( Menu menu ) {
		menu.setPath( request.getContextPath() );
	}
}
