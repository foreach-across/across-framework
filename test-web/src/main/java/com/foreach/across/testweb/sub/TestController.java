package com.foreach.across.testweb.sub;

import com.foreach.across.modules.web.WebTester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController
{
	@Autowired
	private WebTester webTester;

	public TestController() {
		System.out.println("hm");
	}

	@RequestMapping("/")
	@ResponseBody
	public String hello() {
		return "hello from TestController...";
	}
}
