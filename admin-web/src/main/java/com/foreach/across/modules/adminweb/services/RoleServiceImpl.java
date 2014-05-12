package com.foreach.across.modules.adminweb.services;

import com.foreach.across.modules.adminweb.business.Permission;
import com.foreach.across.modules.adminweb.business.Role;
import com.foreach.across.modules.adminweb.repositories.PermissionRepository;
import com.foreach.across.modules.adminweb.repositories.RoleRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

@Service
public class RoleServiceImpl implements RoleService
{
	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void defineRole( String name, String description, Collection<String> permissionNames ) {
		Role role = new Role( name, description );

		Set<Permission> permissions = new TreeSet<>();

		for ( String permissionName : permissionNames ) {
			Permission permission = permissionRepository.getPermission( permissionName );
			Assert.notNull( permission, "Invalid permission: " + permissionName );

			permissions.add( permission );
		}

		role.setPermissions( permissions );

		defineRole( role );
	}

	@Override
	public void defineRole( Role role ) {
		Role existing = roleRepository.getRole( role.getName() );

		if ( existing != null ) {
			existing.setName( role.getName() );
			existing.setDescription( role.getDescription() );
			existing.setPermissions( role.getPermissions() );

			roleRepository.save( existing );

			BeanUtils.copyProperties( existing, role );
		}
		else {
			roleRepository.save( role );
		}
	}

	@Override
	public Collection<Role> getRoles() {
		return roleRepository.getRoles();
	}

	@Override
	public Role getRole( String name ) {
		return roleRepository.getRole( name );
	}

	@Override
	public void save( Role role ) {
		roleRepository.save( role );
	}

	@Override
	public void delete( Role role ) {
		roleRepository.delete( role );
	}
}
