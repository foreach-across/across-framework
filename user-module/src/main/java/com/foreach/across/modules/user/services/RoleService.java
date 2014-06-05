package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.business.Role;

import java.util.Collection;

public interface RoleService
{
	/**
	 * Ensures a Role with the specified properties exists.  The name is the unique key of the role.
	 *
	 * @param name        Name of the Role entity.
	 * @param description Description of the Role.
	 * @param permissions Permission names to apply to the role.
	 */
	void defineRole( String name, String description, Collection<String> permissions );

	/**
	 * Ensures the given Role exists based on the unique name.
	 *
	 * @param role Role entity that should exist.
	 */
	void defineRole( Role role );

	/**
	 * Get all defined Roles.
	 *
	 * @return Collection of Role entities.
	 */
	Collection<Role> getRoles();

	/**
	 * Get the Role entity with the given name.
	 *
	 * @param name Unique name of the Role.
	 * @return Role entity or null;
	 */
	Role getRole( String name );

	/**
	 * Save the given Role entity.
	 *
	 * @param role Entity to save.
	 */
	void save( Role role );

	/**
	 * Delete the given Role entity.
	 *
	 * @param role Entity to delete.
	 */
	void delete( Role role );
}
