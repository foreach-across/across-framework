package com.foreach.across.demoweb.module.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController
{
	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "hello";
	}

	@RequestMapping("/ok")
	@ResponseBody
	public String ok() {
		return "always ok";
	}

	@RequestMapping("/secure/secured")
	@ResponseBody
	@PreAuthorize("hasRole('ROLE_admin')")
	public String secured() {
		return "you can see the secured page";
	}

	@RequestMapping("/secure/fail")
	@ResponseBody
	@PreAuthorize("1 == 0")
	public String shouldNeverSee() {
		return "you should not see this...";
	}

	@RequestMapping("/redirect")
	public String redirect() {
		return "redirect:http://www.google.be";
	}
}
