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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Contains the different settings for the default resource configuration (path, folders, versioning and caching).
 */
@Component
@ConfigurationProperties(prefix = "acrossWebModule.resources")
public class ResourcesConfigurationSettings
{
	protected static final Logger LOG = LoggerFactory.getLogger( ResourcesConfigurationSettings.class );

	@Autowired
	private Environment environment;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AcrossModuleInfo moduleInfo;

	/**
	 * Default resource folders to configure.
	 */
	private String[] folders = new String[] { "js", "css", "static" };

	/**
	 * Relative path for serving all static resources.
	 */
	private String path = AcrossWebModule.DEFAULT_VIEWS_RESOURCES_PATH;

	/**
	 * Auto configure versioning of the default resource resolvers.
	 */
	private boolean versioning = true;

	/**
	 * Auto configure client-side caching of static resources
	 */
	private boolean caching = true;

	/**
	 * Period for client-side resource caching (if enabled).  Defaults to 1 year.
	 */
	private Integer cachingPeriod = 60 * 60 * 24 * 365;

	/**
	 * Fixed version if resource versioning is enabled.  Default will use build number or module version.
	 */
	private String versioningVersion;

	public String[] getFolders() {
		return folders.clone();
	}

	public void setFolders( String[] folders ) {
		Assert.notNull( folders );
		this.folders = folders.clone();
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

	public Integer getCachingPeriod() {
		return cachingPeriod;
	}

	public void setCachingPeriod( Integer cachingPeriod ) {
		this.cachingPeriod = cachingPeriod;
	}

	public String getVersioningVersion() {
		if ( StringUtils.isEmpty( versioningVersion ) ) {
			return environment.getProperty( "build.number", moduleInfo.getVersionInfo().getVersion() );
		}

		return versioningVersion;
	}

	public void setVersioningVersion( String versioningVersion ) {
		this.versioningVersion = versioningVersion;
	}
}
