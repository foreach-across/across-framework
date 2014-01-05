package com.foreach.across.testweb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController
{
	@RequestMapping("/")
	@ResponseBody
	public String hello() {
		return "hello from TestController...";
	}
}
