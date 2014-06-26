package com.foreach.across.test.modules.debugweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import com.foreach.across.test.AcrossTestContextConfigurer;
import com.foreach.across.test.AcrossTestWebConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITDebugWebModule.Config.class)
public class ITDebugWebModule
{
	@Autowired
	@Qualifier("debugWebTemplateRegistry")
	private WebTemplateRegistry debugWebTemplateRegistry;

	@Autowired
	@Qualifier("debugWebResourcePackageManager")
	private WebResourcePackageManager debugWebResourcePackageManager;

	@Autowired
	@Qualifier("debugWebResourceRegistryInterceptor")
	private WebResourceRegistryInterceptor debugWebResourceRegistryInterceptor;

	@Autowired
	@Qualifier("debugHandlerMapping")
	private PrefixingRequestMappingHandlerMapping debugHandlerMapping;

	@Autowired
	private DebugWeb debugWeb;

	@Test
	public void bootstrapModule() {
		assertNotNull( debugWeb );
		assertEquals( "/development/debug", debugWeb.getPathPrefix() );

		assertNotNull( debugWebTemplateRegistry );
		assertNotNull( debugWebResourcePackageManager );
		assertNotNull( debugWebResourceRegistryInterceptor );
		assertNotNull( debugHandlerMapping );
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossTestContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( debugWebModule() );
		}

		private DebugWebModule debugWebModule() {
			DebugWebModule module = new DebugWebModule();
			module.setRootPath( "/development/debug" );
			return module;
		}
	}
}
