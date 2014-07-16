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
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITDisabledWebTemplates.Config.class)
public class ITDisabledWebTemplates
{
	@Autowired(required = false)
	private WebTemplateRegistry webTemplateRegistry;

	@Test
	public void namedWebTemplatesShouldBeRegistered() {
		assertNull( webTemplateRegistry );
	}

	@EnableAcrossContext
	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			webModule.setProperty( AcrossWebModuleSettings.TEMPLATES_ENABLED, false );

			context.addModule( webModule );
		}
	}
}

