package com.foreach.across.modules.adminweb.services;

import com.foreach.across.modules.adminweb.business.Permission;
import com.foreach.across.modules.adminweb.repositories.PermissionRepository;
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
	public void definePermission( String name, String description ) {
		definePermission( new Permission( name, description ) );
	}

	@Transactional
	@Override
	public void definePermission( Permission permission ) {
		Permission existing = permissionRepository.getPermission( permission.getName() );

		if ( existing != null ) {
			existing.setName( permission.getName() );
			existing.setDescription( permission.getDescription() );

			permissionRepository.save( existing );

			BeanUtils.copyProperties( existing, permission );
		}
		else {
			permissionRepository.save( permission );
		}
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
