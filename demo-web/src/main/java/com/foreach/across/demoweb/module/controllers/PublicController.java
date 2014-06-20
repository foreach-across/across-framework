package com.foreach.across.demoweb.module.controllers;

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.security.CurrentUserProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Holds public controller methods - accessible for anonymous users.
 */
@Controller
public class PublicController
{
	@Autowired
	private CurrentUserProxy currentUser;

	@RequestMapping("")
	public String homepage( @AuthenticationPrincipal User user, Model model ) {
		model.addAttribute( "currentUser", currentUser );
		model.addAttribute( "user", user );

		return "th/demoweb/public";
	}
}
