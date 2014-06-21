package com.foreach.across.modules.debugweb.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.debugweb.mvc.DebugMenu;
import com.foreach.across.modules.debugweb.mvc.DebugMenuBuilder;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;

/**
 * Declares a separate handler for debug mappings.
 */
@ComponentScan({ "com.foreach.across.modules.debugweb.controllers" })
@Configuration
@Exposed
public class DebugWebMvcConfiguration extends WebMvcConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger( DebugWebModule.class );

	@Autowired(required = false)
	private WebResourceTranslator viewsWebResourceTranslator;

	@Autowired
	private MenuFactory menuFactory;

	@Autowired
	private DebugWebModule debugWebModule;

	@PostConstruct
	public void initialize() {
		menuFactory.addMenuBuilder( debugMenuBuilder(), DebugMenu.class );
	}

	@Bean
	public DebugMenuBuilder debugMenuBuilder() {
		return new DebugMenuBuilder();
	}

	@Bean
	@Exposed
	public DebugWeb debugWeb() {
		return new DebugWeb( debugWebModule.getRootPath() );
	}

	@Bean
	@Exposed
	public WebTemplateRegistry debugWebTemplateRegistry() {
		WebTemplateRegistry webTemplateRegistry = new WebTemplateRegistry();

		webTemplateRegistry.register( DebugWeb.LAYOUT_TEMPLATE, debugWebLayoutTemplateProcessor() );
		webTemplateRegistry.setDefaultTemplateName( DebugWeb.LAYOUT_TEMPLATE );

		return webTemplateRegistry;
	}

	@Bean
	public LayoutTemplateProcessorAdapterBean debugWebLayoutTemplateProcessor() {
		return new LayoutTemplateProcessorAdapterBean( DebugWeb.LAYOUT_TEMPLATE )
		{
			@Override
			protected void registerWebResources( WebResourceRegistry registry ) {
				registry.addWithKey( WebResource.CSS, DebugWeb.MODULE, DebugWeb.CSS_MAIN, WebResource.VIEWS );
				registry.addWithKey( WebResource.JAVASCRIPT, "jquery",
				                     "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js",
				                     WebResource.EXTERNAL );
				registry.addWithKey( WebResource.CSS, "bootstrap",
				                     "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css",
				                     WebResource.EXTERNAL );
				registry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "bootstrap-js",
				                     "//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js",
				                     WebResource.EXTERNAL );
			}

			@Override
			protected void buildMenus( MenuFactory menuFactory ) {
				menuFactory.buildMenu( DebugMenu.NAME, DebugMenu.class );
			}
		};
	}

	@Bean
	public WebTemplateInterceptor debugWebTemplateInterceptor() {
		return new WebTemplateInterceptor( debugWebTemplateRegistry() );
	}

	@Bean
	@Exposed
	public WebResourcePackageManager debugWebResourcePackageManager() {
		return new WebResourcePackageManager();
	}

	@Bean
	@Exposed
	public WebResourceRegistryInterceptor debugWebResourceRegistryInterceptor() {
		WebResourceRegistryInterceptor interceptor =
				new WebResourceRegistryInterceptor( debugWebResourcePackageManager() );

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
	public PrefixingRequestMappingHandlerMapping debugHandlerMapping() {
		PrefixingRequestMappingHandlerMapping mappingHandlerMapping =
				new PrefixingRequestMappingHandlerMapping( debugWebModule.getRootPath(),
				                                           new AnnotationClassFilter( DebugWebController.class,
				                                                                      true ) );
		mappingHandlerMapping.setInterceptors(
				new Object[] { debugWebResourceRegistryInterceptor(), debugWebTemplateInterceptor() } );

		return mappingHandlerMapping;
	}
}

