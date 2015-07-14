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

package com.foreach.across.core;

import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

import java.util.Set;

/**
 * Default empty AcrossModule with a configurable name. This module does not do anything by default,
 * but can be used to inject in an AcrossContext to satisfy dependencies.
 */
public final class EmptyAcrossModule extends AcrossModule
{
	private final String name;

	public EmptyAcrossModule( String name ) {
		this.name = name;
	}

	/**
	 * @return Name of this module.  Should be unique within a configured AcrossContext.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Description of the content of this module.
	 */
	@Override
	public String getDescription() {
		return "Standard empty module implementation - configurable by name to allow for faking dependencies.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		// No context configurers should be loaded by default
	}
}
