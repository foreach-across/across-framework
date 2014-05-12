package com.foreach.across.modules.adminweb.services;

import com.foreach.across.modules.adminweb.business.Permission;

import java.util.Collection;

public interface PermissionService
{
	/**
	 * Ensures a permission with the given name and description exists.  The name
	 * is the unique identifier of the permission.
	 *
	 * @param name        Unique name of the permission.
	 * @param description Description of the permission.
	 */
	void definePermission( String name, String description );

	/**
	 * Ensures the given permission exists based on the unique name.
	 *
	 * @param permission Permission entity that should exist.
	 */
	void definePermission( Permission permission );

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
