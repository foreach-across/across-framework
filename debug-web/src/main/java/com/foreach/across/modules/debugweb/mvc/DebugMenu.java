package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.web.menu.Menu;
import org.apache.commons.lang3.StringUtils;

public class DebugMenu extends Menu
{
	public static String NAME = "debugMenu";

	private final String rootPath;

	public DebugMenu( String rootPath ) {
		super( NAME );

		setPath( rootPath );
		setTitle( NAME );

		this.rootPath = rootPath;
	}

	@Override
	public Menu addItem( Menu item ) {
		if ( !item.hasUrl() ) {
			String path = item.getPath();

			if ( StringUtils.startsWith( path, "/" ) ) {
				item.setUrl( rootPath + item.getPath() );
			}
			else {
				item.setUrl( rootPath + "/" + item.getPath() );
			}
		}

		return super.addItem( item );
	}
}
