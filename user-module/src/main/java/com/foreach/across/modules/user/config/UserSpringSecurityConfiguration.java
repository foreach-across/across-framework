package com.foreach.across.modules.user.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.spring.security.config.WebSecurityModuleConfigurerAdapter;
import com.foreach.across.modules.user.security.CurrentUserProxy;
import com.foreach.across.modules.user.security.CurrentUserProxyImpl;
import com.foreach.across.modules.user.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registers the UserDetailsService implementation for the User module.
 */
@AcrossDepends(required = "SpringSecurityModule")
@Configuration
public class UserSpringSecurityConfiguration extends WebSecurityModuleConfigurerAdapter
{
	@Autowired
	@Qualifier("userPasswordEncoder")
	private PasswordEncoder passwordEncoder;

	@Bean
	public UserDetailsService userDetailsServiceImpl() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public CurrentUserProxy currentUserProxy() {
		return new CurrentUserProxyImpl();
	}

	@Override
	public void configure( AuthenticationManagerBuilder auth ) throws Exception {
		auth.userDetailsService( userDetailsServiceImpl() ).passwordEncoder( passwordEncoder );
	}
}
