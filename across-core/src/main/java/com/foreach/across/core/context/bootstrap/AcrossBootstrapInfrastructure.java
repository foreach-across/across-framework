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
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import lombok.RequiredArgsConstructor;

/**
 * Helper component that provides access to the specific infrastructure required
 * for bootstrapping an Across context. Resolves or creates the required components
 * when they are first accessed. Strongly tied to the {@link AcrossLifecycleBootstrapHandler}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 * @see AcrossLifecycleBootstrapHandler
 */
@RequiredArgsConstructor
class AcrossBootstrapInfrastructure
{
	private final AcrossContext acrossContext;

	/*
	// bootstrap lock manager
			// parent application context
			// resolve the parent application context
			// expose target
			// module parent context
			// event bus
	 */
}
