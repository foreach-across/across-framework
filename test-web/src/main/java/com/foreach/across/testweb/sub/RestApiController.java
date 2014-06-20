package com.foreach.across.testweb.sub;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RestApiController
{
	@RequestMapping("/test")
	public String notReallyRest() {
		return "Not really rest but if you see this the controller has been picked up.";
	}
}
