package com.foreach.across.testweb.other;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestOtherController
{
	public TestOtherController() {
		System.out.println( "other controller created" );
	}

	@RequestMapping("/other")
	@ResponseBody
	public String hello() {
		return "hello from OTHER controller...";
	}
}
