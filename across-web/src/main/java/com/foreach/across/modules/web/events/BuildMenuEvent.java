package com.foreach.across.modules.web.events;

import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuItem;
import org.apache.commons.lang3.StringUtils;

public class BuildMenuEvent<T extends Menu> implements AcrossEvent
{
	private T menu;

	public BuildMenuEvent( T menu ) {
		this.menu = menu;
	}

	public String getMenuName() {
		return menu.getName();
	}

	public boolean forMenu( String menuName ) {
		return StringUtils.equals( menuName, getMenuName() );
	}

	public T getMenu() {
		return menu;
	}

	public MenuItem getMenuItemWithName( String name ) {
		return menu.getItemWithName( name );
	}

	public MenuItem getMenuItemWithPath( String path ) {
		return menu.getItemWithPath( path );
	}

	public MenuItem addMenuItem( String path, String title ) {
		return menu.addItem( path, title );
	}

	public MenuItem addMenuItem( MenuItem item ) {
		return menu.addItem( item );
	}
}
