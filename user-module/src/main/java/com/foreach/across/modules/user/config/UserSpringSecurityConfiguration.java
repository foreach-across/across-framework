package com.foreach.across.modules.user.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.spring.security.config.WebSecurityModuleConfigurerAdapter;
import com.foreach.across.modules.user.business.CurrentUserProxy;
import com.foreach.across.modules.user.business.CurrentUserProxyImpl;
import com.foreach.across.modules.user.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Registers the UserDetailsService implementation for the User module.
 */
@AcrossDepends(required = "SpringSecurityModule")
@Configuration
public class UserSpringSecurityConfiguration extends WebSecurityModuleConfigurerAdapter
{
	@Bean
	public UserDetailsService userDetailsServiceImpl() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public CurrentUserProxy currentPrincipal() {
		return new CurrentUserProxyImpl();
	}

	@Override
	public void configure( AuthenticationManagerBuilder auth ) throws Exception {
		auth.userDetailsService( userDetailsServiceImpl() );
	}
}
