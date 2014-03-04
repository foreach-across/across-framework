package com.foreach.across.test.modules.exposing;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Exposed
public class ExposingConfiguration
{
	@Bean
	public MyBean beanFromExposingConfiguration() {
		return new MyBean();
	}
}
