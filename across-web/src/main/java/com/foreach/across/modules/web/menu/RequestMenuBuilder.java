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
public class RequestMenuBuilder implements MenuBuilder
{
	@Autowired
	private HttpServletRequest request;

	@SuppressWarnings("unchecked")
	public <T extends Menu> T build( Class<T> menuType ) {
		if ( !menuType.isAssignableFrom( Menu.class ) ) {
			throw new RuntimeException( "Cannot build a menu of " + menuType );
		}

		Menu menu = new Menu();

		setContext( menu );
		return (T) menu;
	}

	public <T extends Menu> BuildMenuEvent<T> buildEvent( T menu ) {
		return new BuildMenuEvent<T>( menu );
	}

	protected void setContext( Menu menu ) {
		menu.setPath( request.getContextPath() );
	}
}
