package com.foreach.across.modules.adminweb.controllers;

import com.foreach.across.modules.adminweb.services.PermissionService;
import com.foreach.across.modules.adminweb.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@AdminWebController
public class RoleController
{
	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleService roleService;

	@RequestMapping("/roles/create")
	public String createRole( Model model ) {
		model.addAttribute( "permissions", permissionService.getPermissions() );

		return "th/adminweb/roles/edit";
	}

	@RequestMapping("/roles")
	public String listRoles( Model model ) {
		model.addAttribute( "roles", roleService.getRoles() );

		return "th/adminweb/roles/list";
	}
}
