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

	public int compare( Menu left, Menu right ) {
		return determineOrder( left ).compareTo( determineOrder( right ) );
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
