package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.events.BuildMenuEvent;

/**
 * Takes care of menu assembly.
 */
public interface MenuBuilder
{
	<T extends Menu> T build( Class<T> menuType );

	<T extends Menu> BuildMenuEvent<T> buildEvent( T menu );
}
