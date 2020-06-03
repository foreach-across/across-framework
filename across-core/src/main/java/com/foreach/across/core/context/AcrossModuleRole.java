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

package com.foreach.across.core.context;

import org.springframework.lang.Nullable;

/**
 * Dependency hints - impact the bootstrapping order of modules.
 */
public enum AcrossModuleRole
{
	APPLICATION,
	INFRASTRUCTURE,
	POSTPROCESSOR;

	/**
	 * Calculates a module priority (as a single {@code long} value) based on a
	 * relative <em>order in module role</em> value.
	 * <p/>
	 * A {@code null} parameter value counts as the default order value ({@code 0}).
	 *
	 * @return priority value
	 */
	public long asPriority( @Nullable Integer orderInModuleRole ) {
		int order = orderInModuleRole != null ? orderInModuleRole : 0;

		switch ( this ) {
			case INFRASTRUCTURE:
				return 10L * Integer.MAX_VALUE + order;
			case APPLICATION:
				return 20L * Integer.MAX_VALUE + order;
			default:
				return 30L * Integer.MAX_VALUE + order;
		}
	}
}
