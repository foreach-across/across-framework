package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.events.AdminWebUrlRegistry;
import com.foreach.across.modules.spring.security.config.WebSecurityModuleConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminWebSecurityConfiguration implements WebSecurityModuleConfigurer
{
	@Autowired
	private AcrossEventPublisher publisher;

	@Autowired
	private AdminWeb adminWeb;

	@Override
	public void configure( AuthenticationManagerBuilder auth ) throws Exception {

	}

	@Override
	public void configure( HttpSecurity http ) throws Exception {
		http.csrf().disable();
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry =
				http.antMatcher( adminWeb.path( "/**" ) ).authorizeRequests();

		publisher.publish( new AdminWebUrlRegistry( adminWeb, urlRegistry ) );

		urlRegistry.anyRequest().authenticated().and().formLogin().defaultSuccessUrl( adminWeb.path( "/" ) ).loginPage(
				adminWeb.path( "/login" ) ).permitAll().and().logout().permitAll();
	}
}
