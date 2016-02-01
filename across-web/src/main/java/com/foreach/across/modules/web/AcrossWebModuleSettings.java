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

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.servlet.MultipartConfigElement;
import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "acrossWebModule")
public class AcrossWebModuleSettings
{
	public static final String TEMPLATES_ENABLED = "acrossWebModule.templates.enabled";
	public static final String TEMPLATES_AUTO_REGISTER = "acrossWebModule.templates.auto-register";

	public static final String MULTIPART_AUTO_CONFIGURE = "acrossWebModule.multipart.auto-configure";
	public static final String MULTIPART_SETTINGS = "acrossWebModule.multipart.settings";

	public static final String VIEWS_RESOURCES_PATH = "acrossWebModule.views.resources";
	public static final String DEVELOPMENT_VIEWS = "acrossWebModule.developmentViews";

	/**
	 * Multipart support configuration settings.
	 */
	private final Multipart multipart = new Multipart();

	/**
	 * Templates configuration.
	 */
	private final Templates templates = new Templates();

	/**
	 * Views configuration.
	 */
	private final Views views = new Views();

	/**
	 * Map of physical locations for views resources.
	 */
	private Map<String, String> developmentViews = Collections.emptyMap();

	public Multipart getMultipart() {
		return multipart;
	}

	public Templates getTemplates() {
		return templates;
	}

	public Views getViews() {
		return views;
	}
	public static final String RESOURCE_URLS_AUTO_CONFIGURE = "acrossWeb.resource.autoconfigure";
	public static final String RESOURCES_VERSION = "acrossWeb.resources.version";

	public static final String DEVELOPMENT_VIEWS = "acrossWeb.development.views";

	public Map<String, String> getDevelopmentViews() {
		return developmentViews;
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

	public void setDevelopmentViews( Map<String, String> developmentViews ) {
		this.developmentViews = developmentViews;
	}

	public static class Multipart
	{
		/**
		 * Auto configure a multipart resolver.
		 */
		private boolean autoConfigure = true;

		/**
		 * MultipartConfigElement holding the multipart upload settings.
		 */
		private MultipartConfigElement settings;

		public boolean isAutoConfigure() {
			return autoConfigure;
		}

		public void setAutoConfigure( boolean autoConfigure ) {
			this.autoConfigure = autoConfigure;
		}

		public MultipartConfigElement getSettings() {
			return settings;
		}

		public void setSettings( MultipartConfigElement settings ) {
			this.settings = settings;
		}
	public boolean isAutoConfigureRecourceUrls() {
		return getProperty( RESOURCE_URLS_AUTO_CONFIGURE, Boolean.class );
	}

	public static class Templates
	{
		/**
		 * True if a default WebTemplateRegistry should be created with support
		 * for WebTemplateProcessors.
		 */
		private boolean enabled = true;

		/**
		 * True if NamedWebTemplateProcessor instances should automatically register themselves
		 * in the registry. Only relevant if templates are enabled.
		 */
		private boolean autoRegister = true;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled( boolean enabled ) {
			this.enabled = enabled;
		}

		public boolean isAutoRegister() {
			return autoRegister;
		}

		public void setAutoRegister( boolean autoRegister ) {
			this.autoRegister = autoRegister;
		}
	}

	public static class Views
	{
		/**
		 * Relative path for serving all static resources.
		 */
		private String resources = AcrossWebModule.DEFAULT_VIEWS_RESOURCES_PATH;

		/**
		 * Should thymeleaf view support be enabled.
		 */
		private boolean thymeleaf = true;

		/**
		 * Should jsp view support be enabled.
		 */
		private boolean jsp = false;

		public String getResources() {
			return resources;
		}

		public void setResources( String resources ) {
			this.resources = resources;
		}

		public boolean isThymeleaf() {
			return thymeleaf;
		}

		public void setThymeleaf( boolean thymeleaf ) {
			this.thymeleaf = thymeleaf;
		}

		public boolean isJsp() {
			return jsp;
		}

		public void setJsp( boolean jsp ) {
			this.jsp = jsp;
		}
	}
}
