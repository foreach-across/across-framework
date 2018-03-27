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

package com.foreach.across.modules.web.menu;

import lombok.NonNull;
import org.springframework.core.OrderComparator;

import java.util.Comparator;

/**
 * Wraps around another comparator to first compare based on explicit order value set,
 * but if the order value is the same, the secondary comparator will be used.
 */
public class OrderComparatorWrapper implements Comparator<Menu>
{
	private final Comparator<Menu> comparator;

	public OrderComparatorWrapper( @NonNull Comparator<Menu> comparator ) {
		this.comparator = comparator;
	}

	public int compare( Menu left, Menu right ) {
		int value = OrderComparator.INSTANCE.compare( left, right );

		if ( value == 0 ) {
			value = comparator.compare( left, right );
		}

		return value;
	}
}
