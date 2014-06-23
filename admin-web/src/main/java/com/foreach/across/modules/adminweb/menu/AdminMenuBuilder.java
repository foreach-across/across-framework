package com.foreach.across.modules.adminweb.menu;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.menu.PrefixContextMenuItemBuilderProcessor;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class AdminMenuBuilder extends RequestMenuBuilder<AdminMenu, AdminMenuEvent>
{
	@Autowired
	private AdminWeb adminWeb;

	@Override
	public AdminMenu build() {
		return new AdminMenu();
	}

	@Override
	public AdminMenuEvent createEvent( AdminMenu menu ) {
		PathBasedMenuBuilder menuBuilder = new PathBasedMenuBuilder( new PrefixContextMenuItemBuilderProcessor(
				adminWeb ) );

		menuBuilder.root( "/" ).title( "Administration" );

		return new AdminMenuEvent( menu, menuBuilder );
	}
}
