package com.foreach.across.modules.adminweb.controllers;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.web.template.ClearTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@AdminWebController
public class AuthenticationController
{
	@Autowired
	private AdminWeb adminWeb;

	@RequestMapping(value = { "", "/" })
	public String dashboard() {
		return "th/adminweb/dashboard";
	}

	@RequestMapping("/login")
	public String login() {
		return "th/adminweb/login";
	}

	@RequestMapping("/logout")
	public String logout( HttpServletRequest request ) throws ServletException {
		request.logout();

		return adminWeb.redirect( "/login?logout" );
	}
}
