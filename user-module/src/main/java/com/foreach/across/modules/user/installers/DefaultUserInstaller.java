package com.foreach.across.modules.user.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.repositories.UserRepository;
import com.foreach.across.modules.user.services.PermissionService;
import com.foreach.across.modules.user.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Installer(description = "Installs the default permissions, roles and user", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class DefaultUserInstaller
{
	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserRepository userRepository;

	@InstallerMethod
	public void install() {
		createPermissionsAndRoles();
		createUser();
	}

	private void createPermissionsAndRoles() {
		permissionService.definePermission( "view users", "View user accounts with limited details" );
		permissionService.definePermission( "manage users", "Manage user accounts" );
		permissionService.definePermission( "manage user roles", "Manage user roles" );

		roleService.defineRole( "admin", "Administrator",
		                        Arrays.asList( "manage users", "view users", "manage user roles" ) );
		roleService.defineRole( "manager", "Manager", Arrays.asList( "view users" ) );
	}

	private void createUser() {
		User user = new User();
		user.setUserName( "admin" );
		user.setPassword( "admin" );
		user.setEmail( "-" );

		userRepository.save( user );
	}
}
