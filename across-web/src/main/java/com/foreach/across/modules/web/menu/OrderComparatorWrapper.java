package com.foreach.across.modules.web.menu;

import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

import java.util.Comparator;

/**
 * Wraps around another comparator to first compare based on explicit order value set,
 * but if the order value is the same, the secondary comparator will be used.
 */
public class OrderComparatorWrapper implements Comparator<Menu>
{
	private static final OrderComparator ORDER_COMPARATOR = new OrderComparator();

	private final Comparator<Menu> comparator;

	public OrderComparatorWrapper( Comparator<Menu> comparator ) {
		Assert.notNull( comparator );
		this.comparator = comparator;
	}

	public int compare( Menu left, Menu right ) {
		int value = ORDER_COMPARATOR.compare( left, right );

		if ( value == 0 ) {
			value = comparator.compare( left, right );
		}

		return value;
	}
}
