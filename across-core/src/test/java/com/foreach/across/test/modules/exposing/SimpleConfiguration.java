package com.foreach.across.test.modules.exposing;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleConfiguration
{
	@Bean
	public MyBean nonExposedBean() {
		return new MyBean();
	}

	@Bean
	@Exposed
	public MyBean exposedBean() {
		return new MyBean();
	}
}
