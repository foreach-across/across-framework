package com.foreach.across.test.filters;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

/**
 * Helper used to filter the beans that should be exposed to the parent context.
 */
public interface BeanFilter
{
	/**
	 * By default all @Service beans are exposed, along with any other beans annotated explicitly with @Exposed.
	 */
	@SuppressWarnings("unchecked")
	public static final BeanFilter DEFAULT = new AnnotationBeanFilter( Service.class, Controller.class, Exposed.class );

	/**
	 * Checks if a bean or its corresponding BeanDefinition match the filter.
	 *
	 * @param bean       Bean instance to check (can be null).
	 * @param definition BeanDefinition corresponding to this bean (can be null).
	 * @return True if the bean and bean definition match.
	 */
	boolean apply( Object bean, BeanDefinition definition );
}
