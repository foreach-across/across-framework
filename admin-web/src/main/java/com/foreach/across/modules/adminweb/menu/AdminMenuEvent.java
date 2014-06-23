package com.foreach.across.modules.adminweb.menu;

import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;

public class AdminMenuEvent extends BuildMenuEvent<AdminMenu>
{
	public AdminMenuEvent( AdminMenu menu, PathBasedMenuBuilder menuBuilder ) {
		super( menu, menuBuilder );
	}
}
