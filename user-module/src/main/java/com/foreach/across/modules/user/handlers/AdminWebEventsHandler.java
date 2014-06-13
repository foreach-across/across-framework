package com.foreach.across.modules.user.handlers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.events.AdminWebUrlRegistry;
import com.foreach.across.modules.adminweb.menu.AdminMenuEvent;
import com.foreach.across.modules.user.business.CurrentUserProxy;
import com.foreach.across.modules.user.controllers.RoleController;
import com.foreach.across.modules.user.controllers.UserController;
import com.foreach.across.modules.web.menu.Menu;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;

@AcrossEventHandler
public class AdminWebEventsHandler
{
	@Autowired
	private AdminWeb adminWeb;

	@Autowired
	private CurrentUserProxy currentUser;

	@Handler
	public void secureUrls( AdminWebUrlRegistry urls ) {
		urls.match( UserController.PATH, UserController.PATH + "/*" ).hasAuthority( "manage users" );
		urls.match( RoleController.PATH, RoleController.PATH + "/*" ).hasAuthority( "manage user roles" );
	}

	@Handler
	public void registerMenu( AdminMenuEvent adminMenuEvent ) {
		Menu menu = new Menu( "User management" );

		if ( currentUser.hasPermission( "manage user roles" ) ) {
			menu.addItem( adminWeb.path( RoleController.PATH ), "Roles" );
		}

		if ( currentUser.hasPermission( "manage users" ) ) {
			menu.addItem( adminWeb.path( UserController.PATH ), "Users" );
		}

		if ( menu.hasItems() ) {
			adminMenuEvent.addItem( menu );
		}
	}
}
