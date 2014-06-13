package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.PermissionGroup;

import java.util.Collection;

public interface PermissionService
{
	/**
	 * Ensures a permission with the given name and description exists.  The name
	 * is the unique identifier of the permission.
	 *
	 * @param name        Unique name of the permission.
	 * @param description Description of the permission.
	 * @param group       Group for the permission.
	 */
	void definePermission( String name, String description, PermissionGroup group );

	/**
	 * Ensures a permission with the given name and description exists.  The name
	 * is the unique identifier of the permission.
	 *
	 * @param name        Unique name of the permission.
	 * @param description Description of the permission.
	 * @param groupName   Name of the permission group the permission should be linked to.
	 */
	void definePermission( String name, String description, String groupName );

	/**
	 * Ensures the given permission exists based on the unique name.
	 *
	 * @param permission Permission entity that should exist.
	 */
	void definePermission( Permission permission );

	/**
	 * Get all defined permission groups.
	 *
	 * @return Collection of PermissionGroup entities.
	 */
	Collection<PermissionGroup> getPermissionGroups();

	/**
	 * Get the PermissionGroup entity by name.
	 *
	 * @param name Unique name of the permission group.
	 * @return PermissionGroup entity of null.
	 */
	PermissionGroup getPermissionGroup( String name );

	/**
	 * Save the PermissionGroup entity.
	 *
	 * @param group Entity to save.
	 */
	void save( PermissionGroup group );

	/**
	 * Delete the PermissionGroup entity.
	 *
	 * @param group Entity to delete.
	 */
	void delete( PermissionGroup group );

	/**
	 * Get all defined permissions.
	 *
	 * @return Collection of Permission entities.
	 */
	Collection<Permission> getPermissions();

	/**
	 * Get the Permission entity by name.
	 *
	 * @param name Unique name of the permission.
	 * @return Permission entity of null.
	 */
	Permission getPermission( String name );

	/**
	 * Save the Permission entity.
	 *
	 * @param permission Entity to save.
	 */
	void save( Permission permission );

	/**
	 * Delete the Permission entity.
	 *
	 * @param permission Entity to delete.
	 */
	void delete( Permission permission );
}
