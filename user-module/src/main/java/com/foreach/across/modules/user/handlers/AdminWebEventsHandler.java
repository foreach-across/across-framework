package com.foreach.across.modules.user.handlers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.adminweb.events.AdminWebUrlRegistry;
import com.foreach.across.modules.adminweb.menu.AdminMenuEvent;
import com.foreach.across.modules.user.controllers.RoleController;
import com.foreach.across.modules.user.controllers.UserController;
import com.foreach.across.modules.user.security.CurrentUserProxy;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;

@AcrossEventHandler
public class AdminWebEventsHandler
{
	@Autowired
	private CurrentUserProxy currentUser;

	@Handler
	public void secureUrls( AdminWebUrlRegistry urls ) {
		urls.match( UserController.PATH, UserController.PATH + "/*" ).hasAuthority( "manage users" );
		urls.match( RoleController.PATH, RoleController.PATH + "/*" ).hasAuthority( "manage user roles" );
	}

	@Handler
	public void registerMenu( AdminMenuEvent adminMenuEvent ) {
		PathBasedMenuBuilder builder = adminMenuEvent.builder();
		builder.group( "/users", "User management" );

		if ( currentUser.hasPermission( "manage users" ) ) {
			builder.item( "/users/users", "Users", UserController.PATH ).order( 1 );
		}

		if ( currentUser.hasPermission( "manage user roles" ) ) {
			builder.item( "/users/roles", "Roles", RoleController.PATH ).order( 2 );
		}
	}
}
