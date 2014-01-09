package com.foreach.across.modules.web.scan;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@EnableWebMvc
@Configuration
public class TestMvcConfiguration
{
	public TestMvcConfiguration() {
		System.out.println("oi mvc");
	}
}
