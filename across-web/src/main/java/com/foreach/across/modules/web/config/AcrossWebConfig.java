package com.foreach.across.modules.web.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.context.AcrossWebArgumentResolver;
import com.foreach.across.modules.web.menu.MenuBuilder;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.menu.MenuStore;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import com.foreach.across.modules.web.resource.WebResourceTranslator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.File;
import java.util.List;
import java.util.Map;

@Configuration
@Exposed
public class AcrossWebConfig extends WebMvcConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebConfig.class );
	private static final String[] DEFAULT_RESOURCES = new String[] { "css", "js" };

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossWebModule acrossWebModule;

	@Override
	public void addResourceHandlers( ResourceHandlerRegistry registry ) {
		for ( String resource : DEFAULT_RESOURCES ) {
			registry.addResourceHandler(
					acrossWebModule.getViewsResourcePath() + "/" + resource + "/**" ).addResourceLocations(
					"classpath:/views/" + resource + "/" );

			if ( acrossWebModule.isDevelopmentMode() ) {
				LOG.info( "Activating development mode resource handlers" );

				for ( Map.Entry<String, String> views : acrossWebModule.getDevelopmentViews().entrySet() ) {
					String url = acrossWebModule.getViewsResourcePath() + "/" + resource + "/" + views.getKey() + "/**";
					String physical = new File( views.getValue(), resource + "/" + views.getKey() ).toURI().toString();

					LOG.debug( "Mapping {} to physical path {}", url, physical );

					registry.addResourceHandler( url ).addResourceLocations( physical );
				}
			}
		}
	}

	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( webResourceRegistryInterceptor() );
	}

	@Override
	public void addArgumentResolvers( List<HandlerMethodArgumentResolver> argumentResolvers ) {
		argumentResolvers.add( acrossWebArgumentResolver() );
	}

	@Bean
	public AcrossWebArgumentResolver acrossWebArgumentResolver() {
		return new AcrossWebArgumentResolver();
	}

	@Bean
	public WebResourcePackageManager webResourcePackageManager() {
		return new WebResourcePackageManager();
	}

	@Bean
	public WebResourceRegistryInterceptor webResourceRegistryInterceptor() {
		return new WebResourceRegistryInterceptor( webResourcePackageManager() );
	}

	@Bean
	public MenuFactory menuFactory( MenuBuilder requestMenuBuilder, MenuStore requestMenuStore ) {
		MenuFactory menuFactory = new MenuFactory();
		menuFactory.setDefaultMenuBuilder( requestMenuBuilder );
		menuFactory.setDefaultMenuStore( requestMenuStore );

		return menuFactory;
	}

	@Bean
	public WebResourceTranslator viewsWebResourceTranslator() {
		if ( acrossWebModule.getViewsResourcePath() != null ) {
			return new WebResourceTranslator()
			{
				public boolean shouldTranslate( WebResource resource ) {
					return StringUtils.equals( WebResource.VIEWS, resource.getLocation() );
				}

				public void translate( WebResource resource ) {
					resource.setLocation( WebResource.RELATIVE );
					resource.setData( acrossWebModule.getViewsResourcePath() + resource.getData() );
				}
			};
		}
		else {
			return null;
		}
	}
}
