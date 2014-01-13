package com.foreach.across.core;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.SpringContextRefreshedEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcrossCoreModuleConfig
{
	@Bean
	@Exposed
	public AcrossEventPublisher eventPublisher() {
		return new AcrossEventPublisher();
	}

	@Bean
	public SpringContextRefreshedEventListener refreshedEventListener() {
		return new SpringContextRefreshedEventListener();
	}
}
