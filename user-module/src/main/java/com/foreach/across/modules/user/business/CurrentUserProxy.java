package com.foreach.across.modules.user.business;

import java.security.Principal;

/**
 * Provides access to the user attached to the request or current thread.
 */
public interface CurrentUserProxy extends Principal
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
