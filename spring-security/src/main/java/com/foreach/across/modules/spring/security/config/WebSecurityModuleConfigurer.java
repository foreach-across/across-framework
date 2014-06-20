package com.foreach.across.modules.spring.security.config;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Deprecated
public interface WebSecurityModuleConfigurer
{
	void configure( AuthenticationManagerBuilder auth ) throws Exception;

	void configure( HttpSecurity http ) throws Exception;
}
