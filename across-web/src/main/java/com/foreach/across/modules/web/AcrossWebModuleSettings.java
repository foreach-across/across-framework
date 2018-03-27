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

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "acrossWebModule")
public class AcrossWebModuleSettings
{
	public static final String TEMPLATES_ENABLED = "acrossWebModule.templates.enabled";
	public static final String TEMPLATES_AUTO_REGISTER = "acrossWebModule.templates.auto-register";

	public static final String DEVELOPMENT_VIEWS = "acrossWebModule.developmentViews";

	public static final String VIEWS_RESOURCES_PATH = "acrossWebModule.resources.path";

	/**
	 * Templates configuration.
	 */
	private final Templates templates = new Templates();

	/**
	 * Map of physical locations for views resources.  Only used if development mode is active.
	 */
	private Map<String, String> developmentViews = Collections.emptyMap();

	public Templates getTemplates() {
		return templates;
	}

	public Map<String, String> getDevelopmentViews() {
		return developmentViews;
	}

	public void setDevelopmentViews( Map<String, String> developmentViews ) {
		this.developmentViews = developmentViews;
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
}
