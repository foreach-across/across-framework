package com.foreach.across.core.filters;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Helper used to filter the beans that should be exposed to the parent context.
 */
public interface BeanFilter
{
	/**
	 * Checks if a bean or its corresponding BeanDefinition match the filter.
	 *
	 * @param beanFactory BeanFactory that owns the bean and definition.
	 * @param beanName    Name of the bean in the beanFactory.
	 * @param bean        Bean instance to check (can be null).
	 * @param definition  BeanDefinition corresponding to this bean (can be null).   @return True if the bean and bean definition match.
	 */
	boolean apply( ConfigurableListableBeanFactory beanFactory,
	               String beanName,
	               Object bean,
	               BeanDefinition definition );
}
