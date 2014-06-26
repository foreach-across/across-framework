package com.foreach.across.test.modules.adminweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
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
@ContextConfiguration(classes = ITAdminWebModule.Config.class)
public class ITAdminWebModule
{
	@Autowired
	@Qualifier("adminWebTemplateRegistry")
	private WebTemplateRegistry adminWebTemplateRegistry;

	@Autowired
	@Qualifier("adminWebResourcePackageManager")
	private WebResourcePackageManager adminWebResourcePackageManager;

	@Autowired
	@Qualifier("adminWebResourceRegistryInterceptor")
	private WebResourceRegistryInterceptor adminWebResourceRegistryInterceptor;

	@Autowired
	@Qualifier("adminRequestMappingHandlerMapping")
	private PrefixingRequestMappingHandlerMapping adminRequestMappingHandlerMapping;

	@Autowired
	private AdminWeb adminWeb;

	@Test
	public void exposedBeans() {
		assertNotNull( adminWeb );
		assertEquals( "/administration", adminWeb.getPathPrefix() );

		assertNotNull( adminWebTemplateRegistry );
		assertNotNull( adminWebResourcePackageManager );
		assertNotNull( adminWebResourceRegistryInterceptor );
		assertNotNull( adminRequestMappingHandlerMapping );
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossTestContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new SpringSecurityModule() );
			context.addModule( adminWebModule() );
		}

		private AdminWebModule adminWebModule() {
			AdminWebModule adminWebModule = new AdminWebModule();
			adminWebModule.setRootPath( "/administration/" );

			return adminWebModule;
		}
	}
}
