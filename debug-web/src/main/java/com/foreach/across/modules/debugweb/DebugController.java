package com.foreach.across.modules.debugweb;

import com.foreach.across.modules.web.WebTester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@DebugWebController
@RequestMapping("/across")
public class DebugController
{
	@Autowired(required = false)
	private WebTester webTester;

	@RequestMapping("/modules")
	@ResponseBody
	public String hello() {
		return "Hello from debug...: " + webTester;
	}
}
