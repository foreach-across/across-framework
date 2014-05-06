package com.foreach.across.modules.adminweb.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.adminweb.services.PermissionService;
import com.foreach.across.modules.adminweb.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Installer(description = "Installs the default permissions and roles for admin web", version = 1,
           phase = InstallerPhase.AfterModuleBootstrap)
public class AdminWebRolesInstaller
{
	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleService roleService;

	@InstallerMethod
	public void createPermissionsAndRoles() {
		permissionService.definePermission( "view users", "View user accounts with limited details" );
		permissionService.definePermission( "manage users", "Manage user accounts" );
		permissionService.definePermission( "manage user roles", "Manage user roles" );

		roleService.defineRole( "admin", "Administrator",
		                        Arrays.asList( "manage users", "view users", "manage user roles" ) );
		roleService.defineRole( "manager", "Manager", Arrays.asList( "view users" ) );
	}
}
