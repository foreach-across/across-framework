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

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Accessors(fluent = true, chain = true)
public class AcrossModuleBootstrapConfigurationSetBuilder
{
	/**
	 * Collection of descriptors for which the configurations should be built.
	 */
	@Setter
	private Collection<AcrossModuleDescriptor> moduleDescriptors;

	/**
	 * Build the collection of {@link AcrossModuleBootstrapConfiguration} instances for the specified {@link #moduleDescriptors}.
	 * This will convert the descriptors to configuration instances, order and merge the extensions together.
	 * Extensions will also be added in relative order.
	 * <p/>
	 * The result is the exact list of module configurations that should be
	 *
	 * @return final
	 */
	public Collection<AcrossModuleBootstrapConfiguration> getConfigurationsInOrder() {
		// convert descriptors to configuration
		// sort the configurations
		// merge the extensions
		// sort the remaining configurations again
		return Collections.emptyList();
	}
}
