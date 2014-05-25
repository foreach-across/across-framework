package com.foreach.across.modules.adminweb.menu;

import com.foreach.across.modules.web.menu.Menu;
import org.apache.commons.lang3.StringUtils;

public class AdminMenu extends Menu
{
	public static final String NAME = "adminMenu";

	private final String rootPath;

	public AdminMenu( String rootPath ) {
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
