package com.foreach.across.modules.adminweb.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.controllers.AuthenticationController;
import com.foreach.across.modules.adminweb.menu.AdminMenu;
import com.foreach.across.modules.adminweb.menu.AdminMenuBuilder;
import com.foreach.across.modules.adminweb.resource.BootstrapWebResourcePackage;
import com.foreach.across.modules.adminweb.resource.JQueryWebResourcePackage;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.modules.web.resource.*;
import com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;

@Configuration
public class AdminWebMvcConfiguration extends WebMvcConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger( AdminWebModule.class );

	@Autowired(required = false)
	private WebResourceTranslator viewsWebResourceTranslator;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AdminWebModule adminWebModule;

	@Autowired
	@Module(AcrossWebModule.NAME)
	private AcrossWebModule acrossWebModule;

	@Autowired
	private MenuFactory menuFactory;

	@PostConstruct
	public void initialize() {
		menuFactory.addMenuBuilder( adminMenuBuilder(), AdminMenu.class );
	}

	@Bean
	public AdminMenuBuilder adminMenuBuilder() {
		return new AdminMenuBuilder();
	}

	@Bean
	@Exposed
	public AdminWeb adminWeb() {
		return new AdminWeb( adminWebModule.getRootPath(), "Administrative web interface" );
	}

	@Bean
	@Exposed
	public WebTemplateRegistry adminWebTemplateRegistry() {
		WebTemplateRegistry webTemplateRegistry = new WebTemplateRegistry();

		webTemplateRegistry.register( AdminWeb.LAYOUT_TEMPLATE, adminLayoutTemplateProcessor() );
		webTemplateRegistry.setDefaultTemplateName( AdminWeb.LAYOUT_TEMPLATE );

		return webTemplateRegistry;
	}

	// todo: verify thymeleaf support is enabled
	@Bean
	public LayoutTemplateProcessorAdapterBean adminLayoutTemplateProcessor() {
		return new LayoutTemplateProcessorAdapterBean( AdminWeb.LAYOUT_TEMPLATE )
		{
			@Override
			protected void registerWebResources( WebResourceRegistry registry ) {
				registry.addPackage( BootstrapWebResourcePackage.NAME );
				registry.addWithKey( WebResource.CSS, AdminWeb.MODULE, AdminWeb.LAYOUT_TEMPLATE_CSS,
				                     WebResource.VIEWS );
			}

			@Override
			protected void buildMenus( MenuFactory menuFactory ) {
				menuFactory.buildMenu( AdminMenu.NAME, AdminMenu.class );
			}
		};
	}

	@Bean
	public WebTemplateInterceptor adminWebTemplateInterceptor() {
		return new WebTemplateInterceptor( adminWebTemplateRegistry() );
	}

	@Bean
	@Exposed
	public WebResourcePackageManager adminWebResourcePackageManager() {
		WebResourcePackageManager webResourcePackageManager = new WebResourcePackageManager();
		webResourcePackageManager.register( JQueryWebResourcePackage.NAME,
		                                    new JQueryWebResourcePackage(
				                                    !acrossWebModule.isDevelopmentMode() ) );
		webResourcePackageManager.register( BootstrapWebResourcePackage.NAME,
		                                    new BootstrapWebResourcePackage(
				                                    !acrossWebModule.isDevelopmentMode() ) );

		return webResourcePackageManager;
	}

	@Bean
	@Exposed
	public WebResourceRegistryInterceptor adminWebResourceRegistryInterceptor() {
		WebResourceRegistryInterceptor interceptor =
				new WebResourceRegistryInterceptor( adminWebResourcePackageManager() );

		if ( viewsWebResourceTranslator != null ) {
			interceptor.addWebResourceTranslator( viewsWebResourceTranslator );
		}
		else {
			LOG.warn( "No default viewsWebResourceTranslator configured - manual translators will be required." );
		}

		return interceptor;
	}

	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping adminRequestMappingHandlerMapping() {
		PrefixingRequestMappingHandlerMapping mappingHandlerMapping =
				new PrefixingRequestMappingHandlerMapping( adminWebModule.getRootPath(),
				                                           new AnnotationClassFilter( AdminWebController.class,
				                                                                      true ) );
		mappingHandlerMapping.setInterceptors(
				new Object[] { adminWebResourceRegistryInterceptor(), adminWebTemplateInterceptor() } );
		return mappingHandlerMapping;
	}

	@Bean
	public AuthenticationController authenticationController() {
		return new AuthenticationController();
	}
}
