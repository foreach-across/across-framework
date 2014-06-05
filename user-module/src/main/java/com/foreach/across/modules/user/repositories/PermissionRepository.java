package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.Permission;

import java.util.Collection;

public interface PermissionRepository
{
	Collection<Permission> getPermissions();

	Permission getPermission( String name );

	void delete( Permission permission );

	void save( Permission permission );
}
