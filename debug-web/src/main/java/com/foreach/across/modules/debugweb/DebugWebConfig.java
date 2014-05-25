package com.foreach.across.modules.debugweb;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.debugweb.handlers.DebugWebEventHandler;
import com.foreach.across.modules.debugweb.mvc.*;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Declares a separate handler for debug mappings.
 */
@ComponentScan({ "com.foreach.across.modules.debugweb.controllers" })
@Configuration
@Exposed
public class DebugWebConfig extends WebMvcConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger( DebugWebModule.class );

	@Autowired(required = false)
	private WebResourceTranslator viewsWebResourceTranslator;

	@Autowired
	private MenuFactory menuFactory;

	@PostConstruct
	public void initialize() {
		menuFactory.addMenuBuilder( debugMenuBuilder(), DebugMenu.class );
	}

	@Override
	public void addArgumentResolvers( List<HandlerMethodArgumentResolver> argumentResolvers ) {
		argumentResolvers.add( debugPageViewArgumentResolver() );
	}

	@Bean
	public DebugMenuBuilder debugMenuBuilder() {
		return new DebugMenuBuilder();
	}

	@Bean
	public DebugPageViewFactory debugPageViewFactory() {
		return new DebugPageViewFactory();
	}

	@Bean
	public DebugPageViewArgumentResolver debugPageViewArgumentResolver() {
		return new DebugPageViewArgumentResolver();
	}

	@Bean
	@Exposed
	public DebugWebEventHandler eventHandler() {
		return new DebugWebEventHandler();
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

		WebResourceRegistry registry = new WebResourceRegistry( debugWebResourcePackageManager() );
		registry.addWithKey( WebResource.CSS, DebugWeb.MODULE, DebugWeb.CSS_MAIN, WebResource.VIEWS );
		interceptor.setDefaultRegistry( registry );

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
	public DebugHandlerMapping debugHandlerMapping() {
		DebugHandlerMapping mapping = new DebugHandlerMapping();
		mapping.setInterceptors( new Object[] { debugWebResourceRegistryInterceptor() } );

		return mapping;
	}
}

