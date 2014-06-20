package com.foreach.across.modules.user.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.security.CurrentUserProxy;
import com.foreach.across.modules.user.security.CurrentUserProxyImpl;
import com.foreach.across.modules.user.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registers the UserDetailsService implementation for the User module.
 */
@AcrossDepends(required = "SpringSecurityModule")
@Configuration
public class UserSpringSecurityConfiguration
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

	@Configuration
	public static class SecConfig /*extends WebSecurityConfigurerAdapter implements Ordered*/
	{
		@Autowired
		private UserDetailsService userDetailsService;

		@Autowired
		private AcrossContextInfo contextInfo;
/*
		@Override
		public int getOrder() {
			return 101;
		}
*/
		@Autowired
		public void configureGlobal( AuthenticationManagerBuilder auth ) throws Exception {
			auth.userDetailsService( userDetailsService ).passwordEncoder(
					AcrossContextUtils.getBeanOfType( contextInfo.getModuleInfo( UserModule.NAME ).getModule(),
					                                  PasswordEncoder.class ) );
		}
	}
	/*
	@Override
	public void configure( AuthenticationManagerBuilder auth ) throws Exception {
		auth.userDetailsService( userDetailsServiceImpl() ).passwordEncoder( passwordEncoder );
	}*/
}
