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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for resource url versioning.
 *
 * @author Arne Vandamme
 */
@Component
@ConfigurationProperties(prefix = "across.web.resources.versioning")
public class ResourceVersioningProperties
{
	@Autowired
	private Environment environment;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AcrossModuleInfo moduleInfo;

	/**
	 * Auto configure versioning of the default resource resolvers.
	 */
	private boolean enabled = true;

	/**
	 * Fixed version if resource versioning is enabled.  Default will use build number or module version.
	 */
	private String version;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public String getVersion() {
		if ( StringUtils.isEmpty( version ) ) {
			return environment.getProperty( "build.number", moduleInfo.getVersionInfo().getVersion() );
		}

		return version;
	}

	public void setVersion( String version ) {
		this.version = version;
	}
}
