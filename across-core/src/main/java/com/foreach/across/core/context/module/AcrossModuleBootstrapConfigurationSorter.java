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

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * Sorts a collection of {@link AcrossModuleBootstrapConfiguration} instances, based on module role, order in role and module dependencies.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@RequiredArgsConstructor
class AcrossModuleBootstrapConfigurationSorter
{
	private final Collection<AcrossModuleBootstrapConfiguration> configurations;

	Collection<AcrossModuleBootstrapConfiguration> getConfigurationsInOrder() {
		// split in groups according to module role
		// ensure that required dependencies are in the same group
		// sort groups according to order in role
		// then according to optional dependencies
		// finally according to required dependencies
		// merge groups in order
		// todo: cyclic dependencies

		return Collections.emptyList();
	}
}
