package com.foreach.across.test.modules.spring.security;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossTestContextConfigurer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITSpringSecurityWithoutWeb.Config.class)
public class ITSpringSecurityWithoutWeb
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void authenticationManagerBuilderShouldExist() {
		AcrossModuleInfo moduleInfo = contextInfo.getModuleInfo( SpringSecurityModule.NAME );

		assertNotNull( moduleInfo );
		assertNotNull( AcrossContextUtils.getBeanOfType( moduleInfo, AuthenticationManagerBuilder.class ) );
	}

	@Configuration
	@AcrossTestConfiguration
	protected static class Config implements AcrossTestContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( springSecurityModule() );
		}

		private SpringSecurityModule springSecurityModule() {
			return new SpringSecurityModule();
		}
	}
}
