package com.foreach.across.test.modules.module1;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Refreshable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Exposed
@Configuration
public class TestModule1Config
{
	@Bean
	public ConstructedBeanModule1 constructedBeanModule1() {
		return new ConstructedBeanModule1( "helloFromModule1" );
	}

	@Bean(name = "refreshable")
	@Refreshable
	public ConstructedBeanModule1 refreshableConstructedBeanModule1() {
		return new ConstructedBeanModule1( "helloFromModule1-refreshable" );
	}
}
