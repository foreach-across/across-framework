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

import org.springframework.core.Ordered;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Registers a fixed order to menu paths that are equal.
 */
public class FixedMenuOrderComparator implements Comparator<Menu>
{
	private Integer defaultOrder = Ordered.LOWEST_PRECEDENCE;

	private Comparator<Menu> fallbackComparator = Menu.SORT_BY_TITLE;
	private Map<MenuMatcher, Integer> orders = new HashMap<>();

	public FixedMenuOrderComparator() {
	}

	public FixedMenuOrderComparator( MenuMatcher... matchers ) {
		for ( int i = 0; i < matchers.length; i++ ) {
			orders.put( matchers[i], i );
		}
	}

	public void put( MenuMatcher matcher, int order ) {
		orders.put( matcher, order );
	}

	public Integer getDefaultOrder() {
		return defaultOrder;
	}

	/**
	 * Setting the default order to null will ensure that the order property of the menu is
	 * being used.
	 *
	 * @param defaultOrder Default order to use if no fixed order set.
	 */
	public void setDefaultOrder( Integer defaultOrder ) {
		this.defaultOrder = defaultOrder;
	}

	public Comparator<Menu> getFallbackComparator() {
		return fallbackComparator;
	}

	/**
	 * Set the comparator that will be used in case the fixed order is the same.
	 *
	 * @param fallbackComparator Comparator instance.
	 */
	public void setFallbackComparator( Comparator<Menu> fallbackComparator ) {
		this.fallbackComparator = fallbackComparator;
	}

	public int compare( Menu left, Menu right ) {
		int value = determineOrder( left ).compareTo( determineOrder( right ) );

		if ( value == 0 && fallbackComparator != null ) {
			value = fallbackComparator.compare( left, right );
		}

		return value;
	}

	private Integer determineOrder( Menu menu ) {
		Integer order = getFixedOrder( menu );

		if ( order != null ) {
			return order;
		}
		else if ( defaultOrder != null ) {
			return defaultOrder;
		}

		return menu.getOrder();
	}

	private Integer getFixedOrder( Menu menu ) {
		for ( Map.Entry<MenuMatcher, Integer> orderEntry : orders.entrySet() ) {
			if ( orderEntry.getKey().matches( menu ) ) {
				return orderEntry.getValue();
			}
		}

		return null;
	}
}
