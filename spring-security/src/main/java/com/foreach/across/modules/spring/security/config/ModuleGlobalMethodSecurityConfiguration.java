package com.foreach.across.modules.spring.security.config;

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

/**
 * Enables Spring method security in modules, ensuring that the same AuthenticationManager is being used.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ModuleGlobalMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration
{
	@Autowired
	private AcrossContextInfo context;

	@Bean
	@Refreshable
	AuthenticationManager delegatingClientAuthenticationManager() {
		return new DelegatingClientAuthenticationManager( context );
	}

	@Override
	protected AuthenticationManager authenticationManager() {
		return delegatingClientAuthenticationManager();
	}

	private static final class DelegatingClientAuthenticationManager implements AuthenticationManager
	{
		private final AcrossContextInfo context;
		private AuthenticationManager delegate;

		private DelegatingClientAuthenticationManager( AcrossContextInfo context ) {
			this.context = context;
		}

		@Override
		public Authentication authenticate( Authentication authentication ) throws AuthenticationException {
			if ( delegate != null ) {
				return delegate.authenticate( authentication );
			}

			return authentication;
		}

		@PostRefresh
		public void refresh() {
			AcrossModuleInfo springSecurityModule = context.getModuleInfo( SpringSecurityModule.NAME );
			Assert.notNull( springSecurityModule );

			delegate = AcrossContextUtils.getBeanOfType( springSecurityModule, AuthenticationManagerBuilder.class )
			                             .getOrBuild();
		}
	}

}
