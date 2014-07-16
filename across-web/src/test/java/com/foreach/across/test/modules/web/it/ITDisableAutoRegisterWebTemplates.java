package com.foreach.across.test.modules.web.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITDisableAutoRegisterWebTemplates.Config.class)
public class ITDisableAutoRegisterWebTemplates
{
	@Autowired(required = false)
	private WebTemplateRegistry webTemplateRegistry;

	@Test
	public void namedWebTemplatesShouldBeRegistered() {
		assertNotNull( webTemplateRegistry );

		assertNull( webTemplateRegistry.get( "default" ) );
		assertNull( webTemplateRegistry.get( "other" ) );
	}

	@EnableAcrossContext
	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			webModule.setProperty( AcrossWebModuleSettings.TEMPLATES_AUTO_REGISTER, false );

			context.addModule( webModule );
		}

		@Bean
		ITAutoRegisterWebTemplates.DefaultWebTemplate defaultWebTemplate() {
			return new ITAutoRegisterWebTemplates.DefaultWebTemplate();
		}

		@Bean
		ITAutoRegisterWebTemplates.OtherWebTemplate otherWebTemplate() {
			return new ITAutoRegisterWebTemplates.OtherWebTemplate();
		}
	}
}
