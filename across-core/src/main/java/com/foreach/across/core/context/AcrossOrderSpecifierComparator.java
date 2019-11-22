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

import com.foreach.across.core.context.support.AcrossOrderSpecifier;
import org.springframework.core.Ordered;

import java.util.*;
import java.util.function.Function;

/**
 * <p>
 * A multi-level comparator for sorting beans scanned from the modules in an AcrossContext.
 * Sorts on the following:
 * <ul>
 * <li>global order (Ordered interface or @Order annotation if none)</li>
 * <li>index of the module the bean belongs to</li>
 * <li>order of the bean in the module (@OrderInModule if any)</li>
 * </ul>
 * </p>
 * <p>To achieve the sorting, the comparator must hold {@link AcrossOrderSpecifier} references for all objects being sorted.</p>
 */
public class AcrossOrderSpecifierComparator implements Comparator<Object>
{
	private static final Function<Object, AcrossOrderSpecifier> DEFAULT_SPECIFIER
			= x -> AcrossOrderSpecifier.forSources( Collections.singletonList( x ) ).build();

	/**
	 * If no order specified, the default is less than lowest priority so it would be
	 * possible to define beans that need to come after all module beans.
	 */
	private static final int DEFAULT_ORDER_IF_UNSPECIFIED = Ordered.LOWEST_PRECEDENCE - 1000;

	private final Map<Object, AcrossOrderSpecifier> orderSpecifierMap = new IdentityHashMap<>();

	/**
	 * Add the order specifier for a particular object.
	 *
	 * @param obj       to add the specifier for
	 * @param specifier specifier
	 */
	public void register( Object obj, AcrossOrderSpecifier specifier ) {
		if ( specifier != null ) {
			orderSpecifierMap.put( obj, specifier );
		}
	}

	@Override
	public int compare( Object left, Object right ) {
		AcrossOrderSpecifier leftSpecifier = orderSpecifierMap.computeIfAbsent( left, DEFAULT_SPECIFIER );
		AcrossOrderSpecifier rightSpecifier = orderSpecifierMap.computeIfAbsent( right, DEFAULT_SPECIFIER );

		Integer leftOrder = leftSpecifier.getOrder( left, DEFAULT_ORDER_IF_UNSPECIFIED );
		Integer rightOrder = rightSpecifier.getOrder( right, DEFAULT_ORDER_IF_UNSPECIFIED );

		int comparison = leftOrder.compareTo( rightOrder );

		if ( comparison == 0 ) {
			Integer leftModuleIndex = leftSpecifier.getModuleIndex( 0 );
			Integer rightModuleIndex = rightSpecifier.getModuleIndex( 0 );

			comparison = leftModuleIndex.compareTo( rightModuleIndex );

			if ( comparison == 0 ) {
				Integer leftOrderInModule = leftSpecifier.getOrderInModule( left, Ordered.LOWEST_PRECEDENCE );
				Integer rightOrderInModule = rightSpecifier.getOrderInModule( right, Ordered.LOWEST_PRECEDENCE );

				comparison = leftOrderInModule.compareTo( rightOrderInModule );
			}
		}

		return comparison;
	}

	/**
	 * Sorts the list of instances according to the comparator.
	 *
	 * @param beans List of instances to sort.
	 */
	public void sort( List<?> beans ) {
		if ( beans.size() > 1 ) {
			beans.sort( this );
		}
	}
}
