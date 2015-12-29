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

package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;

import javax.servlet.MultipartConfigElement;
import java.util.Collections;
import java.util.Map;

public class AcrossWebModuleSettings extends AcrossModuleSettings
{
	/**
	 * True if a default WebTemplateRegistry should be created with support
	 * for WebTemplateProcessors.
	 */
	public static final String TEMPLATES_ENABLED = "acrossWeb.templates.enabled";

	/**
	 * True if NamedWebTemplateProcessor instances should automatically register themselves
	 * in the registry. Only relevant if templates are enabled.
	 */
	public static final String TEMPLATES_AUTO_REGISTER = "acrossWeb.templates.autoregister";

	public static final String MULTIPART_AUTO_CONFIGURE = "acrossWeb.multipart.autoconfigure";

	public static final String MULTIPART_SETTINGS = "acrossWeb.multipart.settings";

	public static final String RESOURCE_URLS_AUTO_CONFIGURE = "acrossWeb.resource.autoconfigure";
	public static final String RESOURCES_VERSION = "acrossWeb.resources.version";

	public static final String DEVELOPMENT_VIEWS = "acrossWeb.development.views";

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
		registry.register( TEMPLATES_ENABLED, Boolean.class, true );
		registry.register( TEMPLATES_AUTO_REGISTER, Boolean.class, true );

		registry.register( MULTIPART_AUTO_CONFIGURE, Boolean.class, true, "Auto configure a multipart resolver." );
		registry.register( MULTIPART_SETTINGS, MultipartConfigElement.class,
		                   null, "MultipartConfigElement holding the multipart upload settings." );

		registry.register( RESOURCE_URLS_AUTO_CONFIGURE, Boolean.class, true, "Auto configure a resource url resolver and relevant filters/interceptors." );
		registry.register( RESOURCES_VERSION, String.class, null, "The version to use for the FixedVersionResolver" );

		registry.register( DEVELOPMENT_VIEWS, Map.class, Collections.<String, String>emptyMap(),
		                   "Map of physical locations for views resources." );
	}

	public boolean isAutoConfigureMultipartResolver() {
		return getProperty( MULTIPART_AUTO_CONFIGURE, Boolean.class );
	}

	public boolean isAutoConfigureRecourceUrls() {
		return getProperty( RESOURCE_URLS_AUTO_CONFIGURE, Boolean.class );
	}

	public boolean isTemplatesEnabled() {
		return getProperty( TEMPLATES_ENABLED, Boolean.class );
	}

	public boolean isAutoRegisterTemplates() {
		return getProperty( TEMPLATES_AUTO_REGISTER, Boolean.class );
	}
}
