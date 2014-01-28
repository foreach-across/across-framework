package com.foreach.across.test.modules.module2;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.AcrossEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestModule2Config
{
	@Bean
	@Exposed
	@AcrossEventHandler
	public ConstructedBeanModule2 constructedBeanModule2() {
		return new ConstructedBeanModule2( "helloFromModule2" );
	}
}
