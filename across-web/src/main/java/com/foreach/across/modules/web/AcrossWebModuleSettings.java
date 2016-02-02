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

	public static final String DEVELOPMENT_VIEWS = "acrossWebModule.developmentViews";

	public static final String VIEWS_RESOURCES_PATH = "acrossWebModule.resources.path";
	public static final String RESOURCE_URLS_AUTO_CONFIGURE = "acrossWebModule.resources.configure-versioning";
	public static final String RESOURCES_VERSION = "acrossWebModule.resources.fixed-version";

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
	 * Resources configuration.
	 */
	private final Resources resources = new Resources();

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

	public Resources getResources() {
		return resources;
	}

	public Views getViews() {
		return views;
	}

	public Map<String, String> getDevelopmentViews() {
		return developmentViews;
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

	public static class Resources
	{
		/**
		 * Relative path for serving all static resources.
		 */
		private String path = AcrossWebModule.DEFAULT_VIEWS_RESOURCES_PATH;

		/**
		 * Auto configure a resource url resolver and relevant filters/interceptors.
		 */
		private boolean configureVersioning;

		/**
		 * The version to use for the {@link org.springframework.web.servlet.resource.FixedVersionStrategy}.
		 */
		private String fixedVersion;

		/**
		 * Duration (seconds) that static resources should be cached.  Defaults to 1 year, put 0 to avoid caching.
		 */
		private Integer cachePeriod = 60 * 60 * 24 * 365;

		public String getPath() {
			return path;
		}

		public void setPath( String path ) {
			this.path = path;
		}

		public boolean isConfigureVersioning() {
			return configureVersioning;
		}

		public void setConfigureVersioning( boolean configureVersioning ) {
			this.configureVersioning = configureVersioning;
		}

		public String getFixedVersion() {
			return fixedVersion;
		}

		public void setFixedVersion( String fixedVersion ) {
			this.fixedVersion = fixedVersion;
		}

		public Integer getCachePeriod() {
			return cachePeriod;
		}

		public void setCachePeriod( Integer cachePeriod ) {
			this.cachePeriod = cachePeriod;
		}
	}

	public static class Views
	{
		/**
		 * Should thymeleaf view support be enabled.
		 */
		private boolean thymeleaf = true;

		/**
		 * Should jsp view support be enabled.
		 */
		private boolean jsp = false;

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
