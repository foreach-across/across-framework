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
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Applies the caching and versioning configuration for the default module resources.
 * Supports the different properties specified in {@link ResourceConfigurationProperties}.
 * <p>
 * Replacing the default behavior can be done by injecting an extension in the same context.
 */
@Configuration
public class DefaultResourceRegistrationConfigurer
{
	/**
	 * Special implementation that does no link transforming inside css files.  This appears
	 * to work better if we use a fixed version strategy, otherwise relative urls inside css
	 * files get transformed incorrectly.
	 * <p>
	 * See https://jira.spring.io/browse/SPR-13727 and https://jira.spring.io/browse/SPR-13806.
	 * </p>
	 * <p>
	 * Implementation is required as there's no way to tell the transformer chain to have
	 * *no* {@link CssLinkResourceTransformer}.  It gets added by default if there is none yet.
	 * </p>
	 */
	protected static class NoOpCssLinkTransformer extends CssLinkResourceTransformer
	{
		@Override
		public Resource transform( HttpServletRequest request,
		                           Resource resource,
		                           ResourceTransformerChain transformerChain ) throws IOException {
			return transformerChain.transform( request, resource );
		}
	}

	@Autowired
	private ResourceCachingProperties resourceCachingProperties;

	@Autowired
	private ResourceVersioningProperties resourceVersioningProperties;

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
			registration.setCacheControl( CacheControl.maxAge( getCachePeriod(), TimeUnit.SECONDS ) );
		}

		if ( shouldApplyFixedVersion() ) {
			registration.resourceChain( cacheResourceResolving() )
			            .addResolver( versionResourceResolver() )
			            .addTransformer( appCacheManifestTransformer() )
			            .addTransformer( new NoOpCssLinkTransformer() );
		}
	}

	/**
	 * @return period to cache resources
	 */
	protected Integer getCachePeriod() {
		return developmentMode.isActive() ? Integer.valueOf( 0 ) : resourceCachingProperties.getPeriod();
	}

	/**
	 * @return configured fixed version to use for versioning strategy
	 */
	protected String getFixedVersion() {
		return developmentMode.isActive() ? developmentMode.getBuildId() : resourceVersioningProperties.getVersion();
	}

	/**
	 * @return true if versioning is enabled
	 */
	protected boolean shouldApplyFixedVersion() {
		return resourceVersioningProperties.isEnabled();
	}

	/**
	 * @return true if a cache  period should be set
	 */
	protected boolean shouldApplyCaching() {
		return developmentMode.isActive() || resourceCachingProperties.isEnabled();
	}

	/**
	 * @return true if resource resolving itself should be cached
	 */
	protected boolean cacheResourceResolving() {
		return !developmentMode.isActive();
	}

	@Bean
	@ConditionalOnProperty(prefix = "acrossWebModule.resources.versioning", value = "enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(value = VersionResourceResolver.class, search = SearchStrategy.CURRENT)
	public VersionResourceResolver versionResourceResolver() {
		return new VersionResourceResolver()
				.addFixedVersionStrategy( getFixedVersion(), "/**" );
	}

	@Bean
	@ConditionalOnProperty(prefix = "acrossWebModule.resources.versioning", value = "enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(value = AppCacheManifestTransformer.class, search = SearchStrategy.CURRENT)
	public AppCacheManifestTransformer appCacheManifestTransformer() {
		return new AppCacheManifestTransformer();
	}
}
