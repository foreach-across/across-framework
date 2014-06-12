package com.foreach.across.modules.user.services;

import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.PermissionGroup;
import com.foreach.across.modules.user.repositories.PermissionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class PermissionServiceImpl implements PermissionService
{
	@Autowired
	private PermissionRepository permissionRepository;

	@Override
	public void definePermission( String name, String description, String groupName ) {
		PermissionGroup group = permissionRepository.getPermissionGroup( groupName );

		if ( group == null ) {
			group = new PermissionGroup();
			group.setName( groupName );

			permissionRepository.save( group );
		}

		definePermission( name, description, group );
	}

	@Override
	public void definePermission( String name, String description, PermissionGroup group ) {
		Permission permission = new Permission( name, description );
		permission.setGroup( group );

		definePermission( permission );
	}

	@Transactional
	@Override
	public void definePermission( Permission permission ) {
		Permission existing = permissionRepository.getPermission( permission.getName() );

		if ( existing != null ) {
			existing.setName( permission.getName() );
			existing.setDescription( permission.getDescription() );
			existing.setGroup( permission.getGroup() );

			permissionRepository.save( existing );

			BeanUtils.copyProperties( existing, permission );
		}
		else {
			permissionRepository.save( permission );
		}
	}

	@Override
	public Collection<PermissionGroup> getPermissionGroups() {
		return permissionRepository.getPermissionGroups();
	}

	@Override
	public PermissionGroup getPermissionGroup( String name ) {
		return permissionRepository.getPermissionGroup( name );
	}

	@Override
	public void save( PermissionGroup group ) {
		permissionRepository.save( group );
	}

	@Override
	public void delete( PermissionGroup group ) {
		permissionRepository.delete( group );
	}

	@Override
	public Collection<Permission> getPermissions() {
		return permissionRepository.getPermissions();
	}

	@Override
	public Permission getPermission( String name ) {
		return permissionRepository.getPermission( name );
	}

	@Override
	public void save( Permission permission ) {
		permissionRepository.save( permission );
	}

	@Override
	public void delete( Permission permission ) {
		permissionRepository.delete( permission );
	}
}
