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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for (client-side) resource caching.
 *
 * @author Arne Vandamme
 */
@Component
@ConfigurationProperties(prefix = "acrossWebModule.resources.caching")
public class ResourceCachingProperties
{
	/**
	 * Auto configure client-side caching of static resources
	 */
	private boolean enabled = true;

	/**
	 * Period for client-side resource caching (if enabled).  Defaults to 1 year.
	 */
	private Integer period = 60 * 60 * 24 * 365;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod( Integer period ) {
		this.period = period;
	}
}
