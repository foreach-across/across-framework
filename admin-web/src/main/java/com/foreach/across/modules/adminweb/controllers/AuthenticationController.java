package com.foreach.across.modules.adminweb.controllers;

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.web.template.ClearTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@AdminWebController
public class AuthenticationController
{
	@RequestMapping("/login")
	@ClearTemplate
	public String login() {
		return "th/adminweb/login";

	}

	@RequestMapping("/logout")
	public String logout( HttpServletRequest request ) throws ServletException {
		request.logout();

		return "redirect:/secure/login";
	}

	@RequestMapping("/redirect")
	public View redirect() {
		return new RedirectView( "http://www.google.be" );
	}

	@RequestMapping("/redirect2")
	public String redirect2() {
		return "redirect:http://www.google.be";
	}
}
