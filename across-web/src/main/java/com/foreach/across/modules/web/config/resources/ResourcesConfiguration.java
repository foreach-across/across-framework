/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.web.config.resources;

import com.foreach.across.condition.ConditionalOnConfigurableServletContext;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurerAdapter;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

import javax.servlet.DispatcherType;
import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Configuration responsible for registering the static resource resolving with optional support
 * for caching and fixed version.
 * <p>
 * In case development mode is active, resources will not be cached and physical paths will be detected.
 * <p>
 * Customizing the default resource configuration can be done by overriding
 * {@link DefaultResourceRegistrationConfigurer}
 * and injecting it back into the module context under the same bean name (<strong>defaultResourceRegistrationConfigurer</strong>.
 *
 * @author Arne Vandamme
 */
@Configuration
public class ResourcesConfiguration extends WebMvcConfigurerAdapter
{
	public static final String RESOURCE_URL_ENCODING_FILTER = "resourceUrlEncodingFilter";

	private static final Logger LOG = LoggerFactory.getLogger( ResourcesConfiguration.class );

	@Autowired
	private ResourceConfigurationProperties configuration;

	@Autowired
	private AcrossDevelopmentMode developmentMode;

	@Autowired
	private DefaultResourceRegistrationConfigurer defaultResourceRegistrationConfigurer;

	@Autowired(required = false)
	private ResourceUrlProviderExposingInterceptor resourceUrlProviderExposingInterceptor;

	@Autowired(required = false)
	private ResourceUrlProvider resourceUrlProvider;

	@Override
	public void addResourceHandlers( ResourceHandlerRegistry registry ) {
		for ( String resourceFolder : configuration.getFolders() ) {
			defaultResourceRegistrationConfigurer.configure(
					resourceFolder,
					registry.addResourceHandler( configuration.getPath() + "/" + resourceFolder + "/**" )
					        .addResourceLocations( "classpath:/views/" + resourceFolder + "/" )
			);

			if ( developmentMode.isActive() ) {
				LOG.info( "Activating {} development mode resource handlers", resourceFolder );

				Map<String, String> views
						= developmentMode.getDevelopmentLocationsForResourcePath( "views/" + resourceFolder );

				for ( Map.Entry<String, String> entry : views.entrySet() ) {
					String url = configuration.getPath() + "/" + resourceFolder + "/" + entry.getKey() + "/**";
					File physical = new File( entry.getValue() );

					LOG.info( "Mapping {} development views for {} to physical path {}",
					          resourceFolder, url, physical );

					defaultResourceRegistrationConfigurer.configure(
							resourceFolder,
							registry.addResourceHandler( url )
							        .addResourceLocations( physical.toURI().toString() )
					);
				}
			}
		}
	}

	/**
	 * Reloads the required resources configuration.
	 *
	 * @param resourceHandlerRegistry containing the registered resources
	 * @param applicationContext      triggering the reload
	 */
	public void reload( com.foreach.across.modules.web.mvc.ResourceHandlerRegistry resourceHandlerRegistry,
	                    ApplicationContext applicationContext,
	                    Map<String, CorsConfiguration> corsConfigurations ) {
		SimpleUrlHandlerMapping resourceHandlerMapping = resourceHandlerMapping();

		if ( resourceUrlProviderExposingInterceptor != null && resourceUrlProvider != null ) {
			resourceHandlerMapping.setInterceptors(
					new HandlerInterceptor[] { resourceUrlProviderExposingInterceptor }
			);
		}
		resourceHandlerMapping.setCorsConfigurations( corsConfigurations );
		resourceHandlerMapping.setUrlMap( resourceHandlerRegistry.getUrlMap() );
		resourceHandlerMapping.initApplicationContext();

		// Detect the handler mappings
		if ( resourceUrlProvider != null ) {
			resourceUrlProvider.onApplicationEvent( new ContextRefreshedEvent( applicationContext ) );
		}
	}

	@Bean
	public SimpleUrlHandlerMapping resourceHandlerMapping() {
		SimpleUrlHandlerMapping resourceHandlerMapping = new SimpleUrlHandlerMapping();
		resourceHandlerMapping.setOrder( 1000 );
		return resourceHandlerMapping;
	}

	@Bean
	@ConditionalOnProperty(prefix = "acrossWebModule.resources.versioning", value = "enabled", matchIfMissing = true)
	@ConditionalOnConfigurableServletContext
	public FilterRegistrationBean resourceUrlEncodingFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setName( RESOURCE_URL_ENCODING_FILTER );
		registration.setFilter( new ResourceUrlEncodingFilter() );
		registration.setAsyncSupported( true );
		registration.setMatchAfter( true );
		registration.setUrlPatterns( Collections.singletonList( "/*" ) );
		registration.setDispatcherTypes( DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC );
		registration.setOrder( Ordered.LOWEST_PRECEDENCE );

		return registration;
	}

	/**
	 * Configures the {@link ResourceUrlProvider} if versioning active.
	 * Ensures the interceptor is registered so all controllers have access to it.
	 */
	@Configuration("resourceUrlProviderExposingInterceptorConfiguration")
	@ConditionalOnProperty(prefix = "acrossWebModule.resources.versioning", value = "enabled", matchIfMissing = true)
	public static class ResourceUrlProviderExposingInterceptorConfiguration extends PrefixingHandlerMappingConfigurerAdapter
	{
		@Override
		public boolean supports( String mapperName ) {
			return true;
		}

		@Override
		public void addInterceptors( InterceptorRegistry registry ) {
			registry.addInterceptor( resourceUrlProviderExposingInterceptor( mvcResourceUrlProvider() ) );
		}

		@Bean
		public ResourceUrlProvider mvcResourceUrlProvider() {
			return new ResourceUrlProvider();
		}

		@Bean
		public ResourceUrlProviderExposingInterceptor resourceUrlProviderExposingInterceptor( ResourceUrlProvider resourceUrlProvider ) {
			return new ResourceUrlProviderExposingInterceptor( resourceUrlProvider );
		}
	}
}
