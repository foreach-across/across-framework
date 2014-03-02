package com.foreach.across.modules.adminweb.repositories;

import com.foreach.across.modules.adminweb.business.Permission;

import java.util.Collection;

public interface PermissionRepository
{
	Collection<Permission> getPermissions();

	Permission getPermission( String name );

	void delete( Permission permission );

	void save( Permission permission );
}
