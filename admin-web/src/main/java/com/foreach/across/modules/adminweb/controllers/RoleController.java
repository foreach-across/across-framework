package com.foreach.across.modules.adminweb.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@AdminWebController
public class RoleController
{
	@RequestMapping("/roles")
	@ResponseBody
	public String bla() {
		return "bla";
	}
}
