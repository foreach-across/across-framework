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

import com.foreach.across.core.development.AcrossDevelopmentMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.FixedVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

/**
 * Applies the caching and versioning configuration for the default module resources.
 * Supports the different properties specified in {@link ResourcesConfigurationSettings}.
 * <p>
 * Replacing the default behavior can be done by injecting an extension in the same context.
 */
@Configuration
public class DefaultResourceRegistrationConfigurer
{
	@Autowired
	private ResourcesConfigurationSettings configuration;

	@Autowired
	private AcrossDevelopmentMode developmentMode;

	/**
	 * Apply the default configuration to a {@link ResourceHandlerRegistration} that configures
	 * a specific type of resource folder (eg. css, js).
	 *
	 * @param resourceFolder name
	 * @param registration   entry
	 */
	public void configure( String resourceFolder, ResourceHandlerRegistration registration ) {
		if ( shouldApplyCaching() ) {
			registration.setCachePeriod( getCachePeriod() );
		}

		if ( shouldApplyFixedVersion() ) {
			registration.resourceChain( cacheResourceResolving() )
			            .addResolver( versionResourceResolver() )
			            .addTransformer( appCacheManifestTransformer() );
		}
	}

	/**
	 * @return period to cache resources
	 */
	protected Integer getCachePeriod() {
		return developmentMode.isActive() ? 0 : configuration.getCachingPeriod();
	}

	/**
	 * @return configured fixed version to use for versioning strategy
	 */
	protected String getFixedVersion() {
		return configuration.getVersioningVersion();
	}

	/**
	 * @return true if versioning is enabled
	 */
	protected boolean shouldApplyFixedVersion() {
		return configuration.isVersioning();
	}

	/**
	 * @return true if a cache  period should be set
	 */
	protected boolean shouldApplyCaching() {
		return developmentMode.isActive() || configuration.isCaching();
	}

	/**
	 * @return true if resource resolving itself should be cached
	 */
	protected boolean cacheResourceResolving() {
		return !developmentMode.isActive();
	}

	@Bean
	@ConditionalOnProperty(value = "acrossWebModule.resources.versioning", matchIfMissing = true)
	@ConditionalOnMissingBean(value = VersionResourceResolver.class, search = SearchStrategy.CURRENT)
	public VersionResourceResolver versionResourceResolver() {
		return new VersionResourceResolver()
				.addVersionStrategy( new FixedVersionStrategy( getFixedVersion() ), "/**" );
	}

	@Bean
	@ConditionalOnProperty(value = "acrossWebModule.resources.versioning", matchIfMissing = true)
	@ConditionalOnMissingBean(value = AppCacheManifestTransformer.class, search = SearchStrategy.CURRENT)
	public AppCacheManifestTransformer appCacheManifestTransformer() {
		return new AppCacheManifestTransformer();
	}
}
