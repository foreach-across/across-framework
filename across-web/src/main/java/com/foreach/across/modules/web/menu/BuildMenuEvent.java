package com.foreach.across.modules.web.menu;

import com.foreach.across.core.events.AcrossEvent;

public class BuildMenuEvent<T extends Menu> implements AcrossEvent
{
	private T menu;

	public BuildMenuEvent( T menu ) {
		this.menu = menu;
	}

	public String getMenuName() {
		return menu.getPath();
	}

	public T getMenu() {
		return menu;
	}

	public void addMenuItem( String path, String title ) {
		menu.addItem( path, title );
	}

	public void addMenuItem( MenuItem item ) {
		menu.addItem( item );
	}
}
