package com.foreach.across.modules.debugweb;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@DebugWebController
@RequestMapping("/across")
public class DebugController
{
	@RequestMapping("/modules")
	@ResponseBody
	public String hello() {
		return "Hello from debug...: ";
	}
}
