package com.foreach.across.modules.user.business;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserProxyImpl implements CurrentUserProxy
{
	@Override
	public long getId() {
		return isAuthenticated() ? getUser().getId() : 0;
	}

	@Override
	public String getEmail() {
		return isAuthenticated() ? getUser().getEmail() : null;
	}

	@Override
	public String getUsername() {
		return isAuthenticated() ? getUser().getUsername() : null;
	}

	@Override
	public String getName() {
		return getUsername();
	}

	@Override
	public boolean hasRole( String name ) {
		return isAuthenticated() && getUser().hasRole( name );
	}

	@Override
	public boolean hasRole( Role role ) {
		return isAuthenticated() && getUser().hasRole( role );
	}

	@Override
	public boolean hasPermission( String name ) {
		return isAuthenticated() && getUser().hasPermission( name );
	}

	@Override
	public boolean hasPermission( Permission permission ) {
		return isAuthenticated() && getUser().hasPermission( permission );
	}

	@Override
	public boolean hasAuthority( String name ) {
		return isAuthenticated() && hasPermission( name );
	}

	@Override
	public User getUser() {
		return isAuthenticated() ? (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
	}

	@Override
	public boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.isAuthenticated() && authentication.getPrincipal() instanceof User;
	}
}
