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

import org.springframework.core.Ordered;

/**
 * Interface that defines the order of an instance within the AcrossModule.  Useful if multiple instances
 * of the same type need to have a specific order within the module, but without influencing the order
 * across the entire context.
 *
 * @author Arne Vandamme
 * @see org.springframework.core.Ordered
 * @see com.foreach.across.core.annotations.OrderInModule
 * @since 1.0.3
 */
public interface OrderedInModule
{
	/**
	 * Default order assigned if no order is specified.
	 */
	int DEFAULT_ORDER = Ordered.LOWEST_PRECEDENCE - 1000;

	/**
	 * @return The order in the module.
	 */
	int getOrderInModule();
}
