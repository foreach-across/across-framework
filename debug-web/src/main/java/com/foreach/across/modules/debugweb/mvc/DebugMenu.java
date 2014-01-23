package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuItem;
import org.apache.commons.lang3.StringUtils;

public class DebugMenu extends Menu
{
	public static String NAME = "debugMenu";

	private final String rootPath;

	public DebugMenu( String rootPath ) {
		super( NAME );

		this.rootPath = rootPath;
	}

	@Override
	public void addItem( MenuItem item ) {
		if ( !item.hasUrl() ) {
			String path = item.getPath();

			if ( StringUtils.startsWith( path, "/" ) ) {
				item.setUrl( rootPath + item.getPath() );
			}
			else {
				item.setUrl( rootPath + "/" + item.getPath() );
			}
		}

		super.addItem( item );
	}
}
