package com.foreach.across.modules.debugweb;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.debugweb.mvc.DebugPageViewArgumentResolver;
import com.foreach.across.modules.debugweb.mvc.DebugPageViewFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Exposed
public class DebugWebConfig
{
	@Bean
	public DebugPageViewFactory debugPageViewFactory() {
		return new DebugPageViewFactory();
	}

	@Bean
	public DebugPageViewArgumentResolver debugPageViewArgumentResolver(){
		return new DebugPageViewArgumentResolver();
	}
}
