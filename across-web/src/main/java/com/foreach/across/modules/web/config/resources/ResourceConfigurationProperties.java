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

import com.foreach.across.modules.web.AcrossWebModule;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the different settings for the default resource configuration (path, folders, versioning and caching).
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "acrossWebModule.resources")
public class ResourceConfigurationProperties
{
	/**
	 * Default resource folders to configure.
	 */
	private String[] folders = new String[] { "js", "css", "static" };

	/**
	 * Relative path for serving all static resources.
	 */
	private String path = AcrossWebModule.DEFAULT_VIEWS_RESOURCES_PATH;

	/**
	 * Relative path for serving all webjars
	 */
	private String webjars = AcrossWebModule.DEFAULT_WEBJARS_PATH;

	public String[] getFolders() {
		return folders.clone();
	}

	public void setFolders( @NonNull String[] folders ) {
		this.folders = folders.clone();
	}

	public String getPath() {
		return path;
	}

	public void setPath( String path ) {
		this.path = path;
	}
}
