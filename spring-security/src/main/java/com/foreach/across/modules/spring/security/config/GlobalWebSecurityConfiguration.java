package com.foreach.across.modules.spring.security.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.registry.RefreshableRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
@AcrossDepends(required = "AcrossWebModule")
@EnableWebMvcSecurity
public class GlobalWebSecurityConfiguration extends WebSecurityConfigurerAdapter
{
	@Bean
	RefreshableRegistry<WebSecurityModuleConfigurer> configurers() {
		return new RefreshableRegistry<>( WebSecurityModuleConfigurer.class, true );
	}

	@Autowired
	public void configureGlobal( AuthenticationManagerBuilder auth ) throws Exception {
		for ( WebSecurityModuleConfigurer configurer : configurers() ) {
			configurer.configure( auth );
		}
	}

	@Override
	protected void configure( HttpSecurity http ) throws Exception {
		for ( WebSecurityModuleConfigurer configurer : configurers() ) {
			configurer.configure( http );
		}
	}
}
