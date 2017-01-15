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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.config.resources.ResourceConfigurationProperties;
import com.foreach.across.modules.web.context.PrefixingPathContext;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.context.PrefixingSupportingWebAppLinkBuilder;
import com.foreach.across.modules.web.context.WebAppLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.springframework.context.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

/**
 * Registers the {@link com.foreach.across.modules.web.context.PrefixingPathRegistry}.
 * <p/>
 * Two default prefixes are registered:
 * <ul>
 * <li><strong>resource</strong>: will resolve to the general resources path</li>
 * <li><strong>static</strong>: will resolve to the static folder under general resources,
 * short-hand for <em>@resource:/static/&lt;path&gt;</em></li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Configuration
public class UrlPrefixingConfiguration
{
	public static final String RESOURCE = "resource";
	public static final String STATIC = "static";

	@Bean
	@Primary
	@Exposed
	public PrefixingPathRegistry prefixingPathRegistry( ResourceConfigurationProperties resourcesConfiguration ) {
		PrefixingPathRegistry prefixingPathRegistry = new PrefixingPathRegistry();
		PrefixingPathContext resourceContext = new PrefixingPathContext( resourcesConfiguration.getPath() );
		prefixingPathRegistry.add( RESOURCE, resourceContext );
		prefixingPathRegistry.add( STATIC, new PrefixingPathContext( resourceContext.getPathPrefix() + "/static" ) );
		return prefixingPathRegistry;
	}

	@Bean
	@Exposed
	@Scope(value = SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
	public WebAppLinkBuilder webAppLinkBuilder( PrefixingPathRegistry prefixingPathRegistry,
	                                            HttpServletRequest request,
	                                            HttpServletResponse response ) {
		PrefixingSupportingWebAppLinkBuilder linkBuilder
				= new PrefixingSupportingWebAppLinkBuilder();
		linkBuilder.setPathResolver( prefixingPathRegistry );
		linkBuilder.setRequest( request );
		linkBuilder.setResponse( response );

		// store the current link builder on the request
		WebResourceUtils.storeLinkBuilder( linkBuilder, request );

		return linkBuilder;
	}
}
