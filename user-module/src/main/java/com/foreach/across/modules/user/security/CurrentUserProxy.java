package com.foreach.across.modules.user.security;

import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.business.User;

/**
 * Provides access to the user attached to the request or current thread.
 */
public interface CurrentUserProxy
{
	long getId();

	String getEmail();

	String getUsername();

	boolean hasRole( String name );

	boolean hasRole( Role role );

	boolean hasPermission( String name );

	boolean hasPermission( Permission permission );

	boolean hasAuthority( String name );

	User getUser();

	boolean isAuthenticated();
}
