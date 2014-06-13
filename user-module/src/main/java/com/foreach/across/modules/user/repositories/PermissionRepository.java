package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.PermissionGroup;

import java.util.Collection;

public interface PermissionRepository
{
	Collection<PermissionGroup> getPermissionGroups();

	Collection<Permission> getPermissions();

	Permission getPermission( String name );

	PermissionGroup getPermissionGroup( String groupName );

	void delete( Permission permission );

	void delete( PermissionGroup permissionGroup );

	void save( PermissionGroup permissionGroup );

	void save( Permission permission );
}
