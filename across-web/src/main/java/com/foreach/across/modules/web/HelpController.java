package com.foreach.across.modules.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelpController
{
	@RequestMapping("/help")
	@ResponseBody
	public String help() {
		return "I need somebody...";
	}
}
