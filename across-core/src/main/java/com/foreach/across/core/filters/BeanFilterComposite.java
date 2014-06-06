package com.foreach.across.core.filters;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Holds any number of BeanFilter implementations and will delegate calls to all of them.
 */
public class BeanFilterComposite implements BeanFilter
{
	private BeanFilter[] filters;

	public BeanFilterComposite( BeanFilter... filters ) {
		this.filters = filters;
	}

	public boolean apply( ConfigurableListableBeanFactory beanFactory, String beanName, Object bean, BeanDefinition definition ) {
		for ( BeanFilter filter : filters ) {
			if ( filter.apply( beanFactory, beanName, bean, definition ) ) {
				return true;
			}
		}
		return false;
	}
}
