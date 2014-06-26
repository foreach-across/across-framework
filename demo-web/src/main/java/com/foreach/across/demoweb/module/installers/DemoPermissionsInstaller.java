package com.foreach.across.demoweb.module.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.user.business.PermissionGroup;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.services.PermissionService;
import com.foreach.across.modules.user.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Installer(description = "Define demo permissions and assign them to the admin role.",
           phase = InstallerPhase.AfterModuleBootstrap)
public class DemoPermissionsInstaller
{
	private static Logger LOG = LoggerFactory.getLogger( DemoPermissionsInstaller.class );

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@InstallerMethod
	public void install() {
		createPermissionGroupAndPermissions();
		assignPermissionsToExistingRole();
	}

	private void createPermissionGroupAndPermissions() {
		// Register the permissions - a default group with these permissions will be created if not found
		permissionService.definePermission( "read something", "The user can read something.", "demo-permissions" );
		permissionService.definePermission( "write something", "The user can write something.", "demo-permissions" );

		// Update the newly created group with some more descriptive text
		PermissionGroup permissionGroup = permissionService.getPermissionGroup( "demo-permissions" );
		permissionGroup.setTitle( "Module: DemoWebModule" );
		permissionGroup.setDescription(
				"Custom permissions defined by the DemoWebModule to illustrate integration with the UserModule." );

		permissionService.save( permissionGroup );
	}

	private void assignPermissionsToExistingRole() {
		// Extend the admin role with the new permissions
		Role role = roleService.getRole( "ROLE_ADMIN" );

		if ( role != null ) {
			role.addPermission( "read something", "write something" );
			roleService.save( role );
		}
		else {
			LOG.warn(
					"ROLE_ADMIN does not appear to exist - the demo permissions have not been assigned to any role." );
		}
	}
}
