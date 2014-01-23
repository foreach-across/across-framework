package com.foreach.across.modules.web.menu;

import java.util.Collection;

public class MenuRenderer
{
	private Menu menu;

	public MenuRenderer( Menu menu ) {
		this.menu = menu;
	}

	public Collection<MenuItem> getItems() {
		return menu.getItems();
	}
}
