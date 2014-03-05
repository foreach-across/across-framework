package com.foreach.across.core.context.beans;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Wrapper to force an instance to be configured as a singleton in a ProvidedBeansMap.
 * Useful if you want to register a BeanDefinition as a singleton instead,
 * or if you want to register both singleton and a corresponding BeanDefinition.
 *
 * @see com.foreach.across.core.context.beans.PrimarySingletonBean
 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
 */
public class SingletonBean
{
	private Object object;
	private BeanDefinition beanDefinition;

	public SingletonBean( Object object ) {
		this.object = object;
	}

	public SingletonBean( Object object, BeanDefinition beanDefinition ) {
		this.object = object;
		this.beanDefinition = beanDefinition;
	}

	public Object getObject() {
		return object;
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	protected void setObject( Object object ) {
		this.object = object;
	}

	protected void setBeanDefinition( BeanDefinition beanDefinition ) {
		this.beanDefinition = beanDefinition;
	}
}
