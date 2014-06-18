package com.foreach.across.demoweb.module.config;

import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class DemoSecurityConfiguration
{
	@Autowired
	public void registerSecurityConfiguration( AcrossContextInfo contextInfo ) {
		contextInfo.getModuleInfo(
				SpringSecurityModule.NAME ).getBootstrapConfiguration().addApplicationContextConfigurer(
				new AnnotatedClassConfigurer( Config.class ) );
	}

	static class Config extends WebSecurityConfigurerAdapter
	{
		@Override
		public void configure( HttpSecurity http ) throws Exception {
			http.antMatcher( "/**" ).authorizeRequests().antMatchers( "/private/**" ).hasAuthority(
					"read something" ).anyRequest().permitAll()/*.rememberMe().key( "sdqfjsdklmjsdfmojiondsijqiosdjodj" )*/;
		}
	}
}
