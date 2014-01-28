package com.foreach.across.modules.web.menu;

public class MenuItem extends Menu
{
	public MenuItem( String path ) {
		super( path );
	}

	public MenuItem( String path, String title ) {
		super( path );

		setTitle( title );
	}
}
