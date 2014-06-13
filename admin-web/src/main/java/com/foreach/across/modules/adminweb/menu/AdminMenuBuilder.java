package com.foreach.across.modules.adminweb.menu;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AdminMenuBuilder extends RequestMenuBuilder<AdminMenu, AdminMenuEvent>
{
	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AdminWebModule adminWebModule;

	@Override
	public AdminMenu build() {
		AdminMenu adminMenu = new AdminMenu( adminWebModule.getRootPath() );
		//setContext( adminMenu );

		return adminMenu;
	}

	@Override
	public AdminMenuEvent createEvent( AdminMenu menu ) {
		return new AdminMenuEvent( menu );
	}
}
