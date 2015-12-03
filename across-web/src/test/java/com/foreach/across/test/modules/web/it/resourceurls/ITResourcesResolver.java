package com.foreach.across.test.modules.web.it.resourceurls;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITResourcesResolver.Config.class)
public class ITResourcesResolver
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void resourceUrlResolverConfigurationShouldBeCreated() {
		ApplicationContext ctx = contextInfo.getModuleInfo( AcrossWebModule.NAME ).getApplicationContext();

		ResourceUrlProvider resourceUrlProvider = ctx.getBean( ResourceUrlProvider.class );
		assertNotNull( resourceUrlProvider );

		ResourceUrlProviderExposingInterceptor interceptor = ctx.getBean( ResourceUrlProviderExposingInterceptor.class );
		assertNotNull( interceptor );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			webModule.setProperty( AcrossWebModuleSettings.RESOURCE_URLS_AUTO_CONFIGURE, true );

			context.addModule( webModule );
		}
	}
}
