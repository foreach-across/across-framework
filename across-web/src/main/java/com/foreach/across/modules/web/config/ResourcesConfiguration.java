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
package com.foreach.across.modules.web.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.development.AcrossDevelopmentMode;
import com.foreach.across.modules.web.AcrossWebModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.FixedVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.io.File;
import java.util.Map;

/**
 * Configuration responsible for registering the static resource resolving with optional support
 * for caching and fixed version.
 * <p>
 * In case development mode is active, resources will not be cached and physical paths will be detected.
 *
 * @author Arne Vandamme
 */
@Configuration
@ConfigurationProperties(prefix = "acrossWebModule.resources")
public class ResourcesConfiguration
{
	protected static final Logger LOG = LoggerFactory.getLogger( ResourcesConfiguration.class );

	@Autowired
	private Environment environment;

	@Autowired
	@Module( AcrossModule.CURRENT_MODULE )
	private AcrossModuleInfo moduleInfo;

	/**
	 * Defaults to js, css
	 */
	private String[] folders = new String[] { "js", "css", "static" };

	/**
	 *
	 */
	private String path = AcrossWebModule.DEFAULT_VIEWS_RESOURCES_PATH;

	private boolean versioning = true;
	private boolean caching = true;

	private Integer cachePeriod = 60 * 60 * 24 * 365;

	private String fixedVersion;

	public String[] getFolders() {
		return folders;
	}

	public void setFolders( String[] folders ) {
		this.folders = folders;
	}

	public String getPath() {
		return path;
	}

	public void setPath( String path ) {
		this.path = path;
	}

	public boolean isVersioning() {
		return versioning;
	}

	public void setVersioning( boolean versioning ) {
		this.versioning = versioning;
	}

	public boolean isCaching() {
		return caching;
	}

	public void setCaching( boolean caching ) {
		this.caching = caching;
	}

	public Integer getCachePeriod() {
		return cachePeriod;
	}

	public void setCachePeriod( Integer cachePeriod ) {
		this.cachePeriod = cachePeriod;
	}

	public String getFixedVersion() {
		if ( StringUtils.isEmpty( fixedVersion ) ) {
			return environment.getProperty( "build.number", moduleInfo.getVersionInfo().getVersion() );
		}

		return fixedVersion;
	}

	public void setFixedVersion( String fixedVersion ) {
		this.fixedVersion = fixedVersion;
	}

	@Configuration
	protected static class RegisterResourceHandlers extends WebMvcConfigurerAdapter
	{
		@Autowired
		private ResourcesConfiguration configuration;

		@Autowired
		private AcrossDevelopmentMode developmentMode;

		@Autowired
		private ResourceRegistrationConfigurer registrationConfigurer;

		@Override
		public void addResourceHandlers( ResourceHandlerRegistry registry ) {
			for ( String resource : configuration.getFolders() ) {
				registrationConfigurer.configure(
						resource,
						registry.addResourceHandler( configuration.getPath() + "/" + resource + "/**" )
						        .addResourceLocations( "classpath:/views/" + resource + "/" )
				);

				if ( developmentMode.isActive() ) {
					LOG.info( "Activating {} development mode resource handlers", resource );

					Map<String, String> views
							= developmentMode.getDevelopmentLocationsForResourcePath( "views/" + resource );

					for ( Map.Entry<String, String> entry : views.entrySet() ) {
						String url = configuration.getPath() + "/" + resource + "/" + entry.getKey() + "/**";
						File physical = new File( entry.getValue() );

						LOG.info( "Mapping {} development views for {} to physical path {}", resource, url, physical );
						registrationConfigurer.configure(
								resource,
								registry.addResourceHandler( url )
								        .addResourceLocations( physical.toURI().toString() )
						);
					}
				}
			}
		}
	}

	@Configuration
	@SuppressWarnings("unused")
	public static class ResourceRegistrationConfigurer
	{
		@Autowired
		private ResourcesConfiguration configuration;

		@Autowired
		private AcrossDevelopmentMode developmentMode;

		protected Integer getCachePeriod() {
			return developmentMode.isActive() || !configuration.isCaching() ? null : configuration.getCachePeriod();
		}

		protected String getFixedVersion() {
			return configuration.getFixedVersion();
		}

		public void configure( String resourceFolder, ResourceHandlerRegistration registration ) {
			registration.setCachePeriod( getCachePeriod() );

			if ( applyFixedVersion() ) {
				registration.resourceChain( getCachePeriod() != null )
				            .addResolver( versionResourceResolver() )
				            .addTransformer( appCacheManifestTransformer() );
			}
		}

		private boolean applyFixedVersion() {
			return configuration.isVersioning();
		}

		@Bean
		@ConditionalOnProperty(value = "acrossWebModule.resources.versioning", matchIfMissing = true)
		public VersionResourceResolver versionResourceResolver() {
			return new VersionResourceResolver()
					.addVersionStrategy( new FixedVersionStrategy( getFixedVersion() ), "/**" );
		}

		@Bean
		@ConditionalOnProperty(value = "acrossWebModule.resources.versioning", matchIfMissing = true)
		public AppCacheManifestTransformer appCacheManifestTransformer() {
			return new AppCacheManifestTransformer();
		}
	}
}
