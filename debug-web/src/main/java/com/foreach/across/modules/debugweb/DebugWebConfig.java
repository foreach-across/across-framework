package com.foreach.across.modules.debugweb;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.debugweb.handlers.DebugWebEventHandler;
import com.foreach.across.modules.debugweb.mvc.DebugHandlerMapping;
import com.foreach.across.modules.debugweb.mvc.DebugPageViewArgumentResolver;
import com.foreach.across.modules.debugweb.mvc.DebugPageViewFactory;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import com.foreach.across.modules.web.resource.WebResourceTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Declares a separate handler for debug mappings.
 */
@ComponentScan({ "com.foreach.across.modules.debugweb.controllers" })
@Configuration
@Exposed
public class DebugWebConfig
{
	private static final Logger LOG = LoggerFactory.getLogger( DebugWebModule.class );

	@Autowired(required = false)
	private WebResourceTranslator viewsWebResourceTranslator;

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
	public WebResourceRegistryInterceptor debugWebResourceRegistryInterceptor() {
		WebResourceRegistryInterceptor interceptor = new WebResourceRegistryInterceptor();

		WebResourceRegistry registry = new WebResourceRegistry();
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

