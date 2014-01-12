package com.foreach.across.modules.web.menu;

import org.springframework.context.ApplicationEvent;

public class MenuConstructEvent extends ApplicationEvent
{
	private MenuFactory owner;
	private Menu menu;

	public MenuConstructEvent( MenuFactory owner, Menu menu ) {
		super( owner );

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
