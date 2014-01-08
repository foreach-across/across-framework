package com.foreach.across.test.modules.module2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestModule2Config
{
	@Bean
	public ConstructedBeanModule2 constructedBeanModule2() {
		return new ConstructedBeanModule2( "helloFromModule2" );
	}
}
