package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.Role;

import java.util.Collection;

public interface RoleRepository
{
	Collection<Role> getRoles();

	Role getRole( String name );

	void delete( Role role );

	void save( Role role );
}
