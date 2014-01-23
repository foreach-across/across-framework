package com.foreach.across.modules.web.menu;

import java.util.LinkedList;

/**
 * Represents a hierarchical menu (tree) structure.
 */
public class Menu
{
	private String path, title, url;
	private LinkedList<MenuItem> items = new LinkedList<MenuItem>();

	public Menu( String path ) {
		this.path = path;
		this.title = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath( String path ) {
		this.path = path;
	}

	public String getUrl() {
		return hasUrl() ? url : path;
	}

	public void setUrl( String url ) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public boolean hasUrl() {
		return url != null;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public LinkedList<MenuItem> getItems() {
		return items;
	}

	public boolean hasItems() {
		return !items.isEmpty();
	}

	public void addItem( String path ) {
		addItem( path, path );
	}

	public void addItem( String path, String title ) {
		MenuItem item = new MenuItem( path );
		item.setTitle( title );

		addItem( item );
	}

	public void addItem( MenuItem item ) {
		items.add( item );
	}

	@Override
	public String toString() {
		return "Menu{" +
				"name='" + path + '\'' +
				", items=" + items +
				'}';
	}
}
