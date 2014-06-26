package com.foreach.across.test.modules.spring.security;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.test.AcrossTestContextConfigurer;
import com.foreach.across.test.AcrossTestWebConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITSpringSecurityWithWeb.Config.class)
public class ITSpringSecurityWithWeb
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired(required = false)
	private FilterChainProxy filterChainProxy;

	@Autowired(required = false)
	private WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator;

	@Autowired(required = false)
	private SecurityExpressionHandler securityExpressionHandler;

	@Autowired(required = false)
	@Qualifier("requestDataValueProcessor")
	private Object requestDataValueProcessor;

	@Test
	public void authenticationManagerBuilderShouldExist() {
		AcrossModuleInfo moduleInfo = contextInfo.getModuleInfo( SpringSecurityModule.NAME );

		assertNotNull( moduleInfo );
		assertNotNull( AcrossContextUtils.getBeanOfType( moduleInfo, AuthenticationManagerBuilder.class ) );
	}

	@Test
	public void exposedBeans() {
		assertNotNull( filterChainProxy );
		assertNotNull( webInvocationPrivilegeEvaluator );
		assertNotNull( securityExpressionHandler );
		assertNotNull( requestDataValueProcessor );
	}

	/**
	 * Add least one web security configurer should be injected in the SpringSecurityModule.
	 */
	@Configuration
	protected static class SecurityConfig extends WebSecurityConfigurerAdapter
	{
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossTestContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( springSecurityModule() );
		}

		private SpringSecurityModule springSecurityModule() {
			SpringSecurityModule module = new SpringSecurityModule();
			module.addApplicationContextConfigurer( SecurityConfig.class );

			return module;
		}
	}
}
