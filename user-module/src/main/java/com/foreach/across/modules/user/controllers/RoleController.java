package com.foreach.across.modules.user.controllers;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.services.PermissionService;
import com.foreach.across.modules.user.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AdminWebController
@RequestMapping(RoleController.PATH)
public class RoleController
{
	public static final String PATH = "/roles";

	@Autowired
	private AdminWeb adminWeb;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleService roleService;

	@RequestMapping("/create")
	public String createRole( Model model ) {
		model.addAttribute( "existing", false );
		model.addAttribute( "role", new Role() );
		model.addAttribute( "permissionGroups", permissionService.getPermissionGroups() );

		return "th/user/roles/edit";
	}

	@RequestMapping
	public String listRoles( Model model ) {
		model.addAttribute( "roles", roleService.getRoles() );

		return "th/user/roles/list";
	}

	@RequestMapping("/{name}")
	public String editRole( @PathVariable("name") String name, Model model ) {
		Role role = roleService.getRole( name );

		model.addAttribute( "existing", true );
		model.addAttribute( "role", role );
		model.addAttribute( "permissionGroups", permissionService.getPermissionGroups() );

		return "th/user/roles/edit";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveRole( @ModelAttribute("role") Role role, RedirectAttributes re ) {
		roleService.save( role );

		re.addAttribute( "roleName", role.getName() );

		return adminWeb.redirect( "/roles/{roleName}" );
	}
}
