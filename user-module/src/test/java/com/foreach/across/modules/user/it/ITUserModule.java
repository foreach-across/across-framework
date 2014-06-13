package com.foreach.across.modules.user.it;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.services.UserService;
import com.foreach.across.test.AcrossTestContextConfiguration;
import com.foreach.across.test.AcrossTestContextConfigurer;
import org.hibernate.cfg.AvailableSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITUserModule.Config.class)
public class ITUserModule
{
	@Autowired
	private UserService userService;

	@Test
	public void verifyBootstrapped() {
		assertNotNull( userService );
		assertNotNull( userService.getUserByUsername( "admin" ) );
	}

	@Configuration
	@Import(AcrossTestContextConfiguration.class)
	static class Config implements AcrossTestContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( acrossHibernateModule() );
			context.addModule( userModule() );
		}

		private AcrossHibernateModule acrossHibernateModule() {
			AcrossHibernateModule hibernateModule = new AcrossHibernateModule();
			hibernateModule.setHibernateProperty( AvailableSettings.HBM2DDL_AUTO, "validate" );

			return hibernateModule;
		}

		private UserModule userModule() {
			return new UserModule();
		}
	}
}
