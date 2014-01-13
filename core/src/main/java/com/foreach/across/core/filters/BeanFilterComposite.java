package com.foreach.across.core.filters;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Holds any number of BeanFilter implementations and will delegate calls to all of them.
 */
public class BeanFilterComposite implements BeanFilter
{
	private BeanFilter[] filters;

	public BeanFilterComposite( BeanFilter... filters ) {
		this.filters = filters;
	}

	/**
	 * Will delegate to all filters until one matches.  Will only return false if none
	 * of the filters have returned true.
	 *
	 * @param bean       Bean instance to check (can be null).
	 * @param definition BeanDefinition corresponding to this bean (can be null).
	 * @return True if any of the filters match.
	 */
	public boolean apply( Object bean, BeanDefinition definition ) {
		for ( BeanFilter filter : filters ) {
			if ( filter.apply( bean, definition ) ) {
				return true;
			}
		}
		return false;
	}
}
