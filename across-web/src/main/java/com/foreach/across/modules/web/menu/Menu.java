package com.foreach.across.modules.web.menu;

import java.util.LinkedList;
import java.util.List;

public class Menu
{
	private String name;
	private LinkedList<MenuItem> items = new LinkedList<MenuItem>();

	public Menu( String name ) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public LinkedList<MenuItem> getItems() {
		return items;
	}

	public boolean hasItems() {
		return !items.isEmpty();
	}

	public void addItem( String name ) {
		items.add( new MenuItem( name ) );
	}

	@Override
	public String toString() {
		return "Menu{" +
				"name='" + name + '\'' +
				", items=" + items +
				'}';
	}
}
