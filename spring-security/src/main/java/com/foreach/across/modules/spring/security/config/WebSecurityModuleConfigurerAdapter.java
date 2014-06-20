package com.foreach.across.modules.spring.security.config;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Deprecated
public abstract class WebSecurityModuleConfigurerAdapter implements WebSecurityModuleConfigurer
{
	@Override
	public void configure( AuthenticationManagerBuilder auth ) throws Exception {
	}

	@Override
	public void configure( HttpSecurity http ) throws Exception {
	}
}
