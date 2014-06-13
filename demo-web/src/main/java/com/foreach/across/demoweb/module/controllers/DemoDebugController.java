package com.foreach.across.demoweb.module.controllers;

import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import org.springframework.web.bind.annotation.RequestMapping;

@DebugWebController
public class DemoDebugController
{
	@RequestMapping("/redirect")
	public String redirect() {
		return "redirect:http://www.google.be";
	}
}
