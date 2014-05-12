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
