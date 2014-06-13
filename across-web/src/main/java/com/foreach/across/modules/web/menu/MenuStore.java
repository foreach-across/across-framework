package com.foreach.across.modules.web.menu;

/**
 * Stores all created menus.
 */
public interface MenuStore
{
	void save( String name, Menu menu );

	Menu get( String name );

	<T extends Menu> T get( String name, Class<T> menuType );
}
