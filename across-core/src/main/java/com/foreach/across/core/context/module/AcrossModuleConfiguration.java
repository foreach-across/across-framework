/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.module;

import java.util.Collection;

/**
 * Represents the actual configuration that will be bootstrapped for a module.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
public interface AcrossModuleConfiguration
{
	/**
	 * The original {@link AcrossModuleDescriptor} that this configuration was created from.
	 * Will hold the original configuration settings, before any extensions or customizations.
	 *
	 * @return descriptor
	 */
	AcrossModuleDescriptor getModuleDescriptor();

	/**
	 * Collection of extension configurations that have been added to this module.
	 *
	 * @return collection of extension configurations
	 */
	Collection<AcrossModuleConfiguration> getExtensions();
	//isEmpty()
}
