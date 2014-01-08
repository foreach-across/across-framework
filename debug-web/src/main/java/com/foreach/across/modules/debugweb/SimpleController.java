package com.foreach.across.modules.debugweb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SimpleController
{
	@RequestMapping("/check")
	@ResponseBody
	public String check() {
		return "checked...";
	}
}
