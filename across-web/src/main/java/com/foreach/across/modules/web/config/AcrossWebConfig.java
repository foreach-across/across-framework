package com.foreach.across.modules.web.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
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
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Configuration
@Exposed
public class AcrossWebConfig extends WebMvcConfigurerAdapter
{
	private static final String VIEWS_PROPERTY_PREFIX = "acrossWeb.views.";

	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebConfig.class );
	private static final String[] DEFAULT_RESOURCES = new String[] { "css", "js" };

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossWebModule acrossWebModule;

	@Autowired
	private AcrossWebModuleSettings settings;

	@Override
	public void addResourceHandlers( ResourceHandlerRegistry registry ) {
		Map<String, String> developmentViews = Collections.emptyMap();

		if ( acrossWebModule.isDevelopmentMode() ) {
			developmentViews = loadDevelopmentViews();
		}

		for ( String resource : DEFAULT_RESOURCES ) {
			registry.addResourceHandler(
					acrossWebModule.getViewsResourcePath() + "/" + resource + "/**" ).addResourceLocations(
					"classpath:/views/" + resource + "/" );

			if ( acrossWebModule.isDevelopmentMode() ) {
				LOG.info( "Activating {} development mode resource handlers", resource );

				for ( Map.Entry<String, String> entry : developmentViews.entrySet() ) {
					String url = acrossWebModule.getViewsResourcePath() + "/" + resource + "/" + entry.getKey() + "/**";
					File physical = new File( entry.getValue(), resource + "/" + entry.getKey() );

					if ( physical.exists() ) {
						LOG.info( "Mapping {} development views for {} to physical path {}", resource, url, physical );
						registry.addResourceHandler( url )
						        .addResourceLocations( physical.toURI().toString() );
					}
					else {
						LOG.warn( "Ignoring {} development views for {} since location {} does not exist",
						          resource, entry.getKey(), physical );
					}
				}
			}
		}
	}

	private Map<String, String> loadDevelopmentViews() {
		Map<String, String> views = new HashMap<>();

		// Fetch from properties
		String propertiesFilePath = settings.resolvePlaceholders(
				settings.getProperty( AcrossWebModuleSettings.DEVELOPMENT_VIEWS_PROPERTIES_LOCATION )
		);

		File propertiesFile = new File( propertiesFilePath );

		if ( propertiesFile.exists() ) {
			LOG.info( "Loading development views properties from {}", propertiesFile );

			Properties props = new Properties();
			try (FileInputStream fis = new FileInputStream( propertiesFile )) {
				props.load( fis );

				for ( Map.Entry<Object, Object> entry : props.entrySet() ) {
					String propertyName = (String) entry.getKey();

					if ( propertyName.startsWith( VIEWS_PROPERTY_PREFIX ) ) {
						String module = propertyName.replace( VIEWS_PROPERTY_PREFIX, "" );

						views.put( module, (String) entry.getValue() );
					}
				}
			}
			catch ( IOException ioe ) {
				LOG.warn( "Failed to load development views from {}", propertiesFile, ioe );
			}
		}

		// Override with entries configured directly set entries
		views.putAll( acrossWebModule.getDevelopmentViews() );

		return views;
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
