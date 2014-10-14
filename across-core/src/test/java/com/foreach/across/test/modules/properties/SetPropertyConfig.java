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

package com.foreach.across.test.modules.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SetPropertyConfig
{
	@Value("${contextValue}")
	public String contextValue;

	@Value("${moduleSourceValue}")
	public String moduleSourceValue;

	@Value("${moduleDirectValue}")
	public String moduleDirectValue;

	@Value("${contextDirectValue}")
	public int contextDirectValue;

	@Value("${unresolvable:50}")
	public long unresolvable;

	@Autowired
	private PropertiesModuleSettings settings;

	public String getProperty( String propertyName ) {
		return settings.getProperty( propertyName );
	}

	public <T> T getProperty( String propertyName, Class<T> propertyClass ) {
		return settings.getProperty( propertyName, propertyClass );
	}
}
