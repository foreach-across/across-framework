package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.AdminWebModuleSettings;
import com.foreach.across.modules.adminweb.events.AdminWebUrlRegistry;
import com.foreach.across.modules.spring.security.config.WebSecurityModuleConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
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

	@Autowired
	private Environment environment;

	@Override
	public void configure( AuthenticationManagerBuilder auth ) throws Exception {

	}

	@Override
	public void configure( HttpSecurity root ) throws Exception {
		HttpSecurity http = root.antMatcher( adminWeb.path( "/**" ) );

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry =
				http.authorizeRequests();

		publisher.publish( new AdminWebUrlRegistry( adminWeb, urlRegistry ) );

		// Only users with the "access administration " permission can login
		urlRegistry.anyRequest().hasAuthority( "access administration" ).and().formLogin().defaultSuccessUrl(
				adminWeb.path( "/" ) ).loginPage( adminWeb.path( "/login" ) ).permitAll().and().logout().permitAll();

		configureRememberMe( http );
	}

	private void configureRememberMe( HttpSecurity http ) throws Exception {
		if ( adminWeb.getSettings().isRememberMeEnabled() ) {
			String rememberMeKey = environment.getProperty( AdminWebModuleSettings.REMEMBER_ME_KEY, "" );
			int rememberMeValiditySeconds =
					environment.getProperty( AdminWebModuleSettings.REMEMBER_ME_TOKEN_VALIDITY_SECONDS, Integer.class,
					                         259200 );

			http.rememberMe().key( rememberMeKey ).tokenValiditySeconds( rememberMeValiditySeconds );
		}
	}
}
