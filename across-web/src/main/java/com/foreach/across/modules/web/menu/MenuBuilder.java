package com.foreach.across.modules.web.menu;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Takes care of menu assembly.
 */
public interface MenuBuilder
{
	<T extends Menu> T build( Class<T> menuType );

	<T extends Menu> BuildMenuEvent<T> buildEvent( T menu );
}
