package com.foreach.across.testweb.sub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController
{
	@RequestMapping("/")
	public String hello() {
		return "sub/test";
	}
}
