package com.foreach.across.modules.adminweb.menu;

import com.foreach.across.modules.web.events.BuildMenuEvent;

public class AdminMenuEvent extends BuildMenuEvent<AdminMenu>
{
	public AdminMenuEvent( AdminMenu menu ) {
		super( menu );
	}
}
