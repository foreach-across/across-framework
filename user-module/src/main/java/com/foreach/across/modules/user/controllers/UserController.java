package com.foreach.across.modules.user.controllers;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.user.dto.UserDto;
import com.foreach.across.modules.user.services.RoleService;
import com.foreach.across.modules.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AdminWebController
@RequestMapping(UserController.PATH)
public class UserController
{
	public static final String PATH = "/users";

	@Autowired
	private AdminWeb adminWeb;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@RequestMapping
	public String listUsers( Model model ) {
		model.addAttribute( "users", userService.getUsers() );

		return "th/user/users/list";
	}

	@RequestMapping("/create")
	public String createUser( Model model ) {
		model.addAttribute( "existing", false );
		model.addAttribute( "user", new UserDto() );
		model.addAttribute( "roles", roleService.getRoles() );

		return "th/user/users/edit";
	}

	@RequestMapping("/{id}")
	public String editUser( @PathVariable("id") long id, Model model ) {
		UserDto user = userService.createUserDto( userService.getUserById( id ) );

		model.addAttribute( "existing", true );
		model.addAttribute( "user", user );
		model.addAttribute( "roles", roleService.getRoles() );

		return "th/user/users/edit";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveUser( @ModelAttribute("user") UserDto user, RedirectAttributes re ) {
		userService.save( user );

		re.addAttribute( "userId", user.getId() );

		return adminWeb.redirect( "/users/{userId}" );
	}
}
