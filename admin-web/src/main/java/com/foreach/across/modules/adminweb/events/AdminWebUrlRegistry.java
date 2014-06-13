package com.foreach.across.modules.adminweb.events;

import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.modules.adminweb.AdminWeb;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

public class AdminWebUrlRegistry implements AcrossEvent
{
	private final AdminWeb adminWeb;
	private final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry;

	public AdminWebUrlRegistry( AdminWeb adminWeb,
	                            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry ) {
		this.adminWeb = adminWeb;
		this.registry = registry;
	}

	public ExpressionUrlAuthorizationConfigurer.AuthorizedUrl match( String... antPatterns ) {
		String[] prefixed = new String[antPatterns.length];

		for ( int i = 0; i < antPatterns.length; i++ ) {
			prefixed[i] = adminWeb.path( antPatterns[i] );
		}

		return registry.antMatchers( prefixed );
	}

	public ExpressionUrlAuthorizationConfigurer.AuthorizedUrl match( HttpMethod httpMethod, String... antPatterns ) {
		String[] prefixed = new String[antPatterns.length];

		for ( int i = 0; i < antPatterns.length; i++ ) {
			prefixed[i] = adminWeb.path( antPatterns[i] );
		}

		return registry.antMatchers( httpMethod, prefixed );
	}

	public ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry getRegistry() {
		return registry;
	}
}
