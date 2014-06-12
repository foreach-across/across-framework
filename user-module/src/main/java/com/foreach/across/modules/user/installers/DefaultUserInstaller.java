package com.foreach.across.modules.user.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.business.PermissionGroup;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.business.User;
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
		createPermissionGroups();
		createPermissionsAndRoles();
		createUser();
	}

	private void createPermissionGroups() {
		PermissionGroup group = permissionService.getPermissionGroup( UserModule.NAME );

		if ( group == null ) {
			group = new PermissionGroup();
			group.setName( UserModule.NAME );
			group.setTitle( "Module: " + UserModule.NAME );
			group.setDescription( "Basic user and user management related permissions." );

			permissionService.save( group );
		}
	}

	private void createPermissionsAndRoles() {
		permissionService.definePermission( "access administration",
		                                    "User can perform one or more administrative actions.  Usually this means the user can access the administration interface.",
		                                    UserModule.NAME );

		permissionService.definePermission( "manage users", "Manage user accounts", UserModule.NAME );
		permissionService.definePermission( "manage user roles", "Manage user roles", UserModule.NAME );

		roleService.defineRole( "ROLE_ADMIN", "Administrator",
		                        Arrays.asList( "access administration", "manage users", "manage user roles" ) );
		roleService.defineRole( "ROLE_MANAGER", "Manager", Arrays.asList( "access administration", "manage users" ) );
	}

	private void createUser() {
		User existing = userService.getUserByUsername( "admin" );

		if ( existing == null ) {
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
}
