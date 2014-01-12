package com.foreach.across.testweb.other;

import org.springframework.stereotype.Component;

@Component
public class HelperBean
{
	private String helloFromHelper = "hello from helper";

	public String getHelloFromHelper() {
		return helloFromHelper;
	}
}
