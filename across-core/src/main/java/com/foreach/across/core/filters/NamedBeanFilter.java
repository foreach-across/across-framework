package com.foreach.across.core.filters;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Filters beans on a set of allowed bean names.
 */
public class NamedBeanFilter implements BeanFilter
{
	private final String[] allowedNames;

	public NamedBeanFilter( String... allowedNames ) {
		this.allowedNames = allowedNames;
	}

	@Override
	public boolean apply( ConfigurableListableBeanFactory beanFactory,
	                      String beanName,
	                      Object bean,
	                      BeanDefinition definition ) {
		return ArrayUtils.contains( allowedNames, beanName );
	}
}
