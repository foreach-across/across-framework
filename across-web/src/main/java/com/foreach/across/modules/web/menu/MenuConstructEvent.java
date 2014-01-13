package com.foreach.across.modules.web.menu;

import com.foreach.across.core.events.AcrossEvent;

public class MenuConstructEvent implements AcrossEvent
{
	private MenuFactory owner;
	private Menu menu;

	public MenuConstructEvent( MenuFactory owner, Menu menu ) {
		this.owner = owner;
		this.menu = menu;
	}

	public String getMenuName() {
		return menu.getName();
	}

	public MenuFactory getOwner() {
		return owner;
	}

	public Menu getMenu() {
		return menu;
	}
}
