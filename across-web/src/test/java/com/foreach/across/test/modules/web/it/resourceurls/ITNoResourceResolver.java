package com.foreach.across.test.modules.web.it.resourceurls;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITNoResourceResolver.Config.class)
public class ITNoResourceResolver
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void resourceUrlResolverConfigurationShouldNotBeCreated() {
		ApplicationContext ctx = contextInfo.getModuleInfo( AcrossWebModule.NAME ).getApplicationContext();

		try {
			ResourceUrlProvider resourceUrlProvider = ctx.getBean( ResourceUrlProvider.class );
			assertNull( resourceUrlProvider );
		} catch ( NoSuchBeanDefinitionException e ) {
			assertTrue( true );
		}

		try {
			ResourceUrlProviderExposingInterceptor interceptor = ctx.getBean( ResourceUrlProviderExposingInterceptor.class );
			assertNull( interceptor );
		} catch ( NoSuchBeanDefinitionException e ) {
			assertTrue( true );
		}
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			AcrossWebModule webModule = new AcrossWebModule();
			webModule.setProperty( AcrossWebModuleSettings.RESOURCE_URLS_AUTO_CONFIGURE, false );

			context.addModule( webModule );
		}
	}
}
