package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.events.BuildMenuEvent;

/**
 * Takes care of menu assembly.
 */
public interface MenuBuilder<T extends Menu, E extends BuildMenuEvent<T>>
{
	T build();

	E buildEvent( T menu );
}
