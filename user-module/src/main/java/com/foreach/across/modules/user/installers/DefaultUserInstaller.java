package com.foreach.across.modules.user.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.dto.UserDto;
import com.foreach.across.modules.user.services.PermissionService;
import com.foreach.across.modules.user.services.RoleService;
import com.foreach.across.modules.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;

@Installer(description = "Installs the default permissions, roles and user", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class DefaultUserInstaller
{
	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserService userService;

	@InstallerMethod
	public void install() {
		createPermissionsAndRoles();
		createUser();
	}

	private void createPermissionsAndRoles() {
		permissionService.definePermission( "manage users", "Manage user accounts" );
		permissionService.definePermission( "manage user roles", "Manage user roles" );

		roleService.defineRole( "ROLE_ADMIN", "Administrator", Arrays.asList( "manage users", "manage user roles" ) );
		roleService.defineRole( "ROLE_MANAGER", "Manager", Arrays.asList( "manage users" ) );
	}

	private void createUser() {
		UserDto user = new UserDto();
		user.setUsername( "admin" );
		user.setPassword( "admin" );
		user.setEmail( "-" );

		HashSet<Role> roles = new HashSet<>();
		roles.add( roleService.getRole( "ROLE_ADMIN" ) );

		user.setRoles( roles );

		userService.save( user );
	}
}
