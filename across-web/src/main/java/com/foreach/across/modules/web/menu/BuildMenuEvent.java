package com.foreach.across.modules.web.menu;

import com.foreach.across.core.events.AcrossEvent;

public class BuildMenuEvent<T extends Menu> implements AcrossEvent
{
	private MenuFactory owner;
	private T menu;

	public BuildMenuEvent( MenuFactory owner, T menu ) {
		this.owner = owner;
		this.menu = menu;
	}

	public String getMenuName() {
		return menu.getPath();
	}

	public MenuFactory getOwner() {
		return owner;
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
