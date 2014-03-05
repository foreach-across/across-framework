package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.PrimarySingletonBean;
import com.foreach.across.core.context.beans.SingletonBean;

/**
 * Simple configurer to provide a singleton bean.
 */
public class SingletonBeanConfigurer extends ProvidedBeansConfigurer
{
	public SingletonBeanConfigurer( String beanName, Object value ) {
		this( beanName, value, false );
	}

	public SingletonBeanConfigurer( String beanName, Object value, boolean makePrimary ) {
		addBean( beanName, makePrimary ? new PrimarySingletonBean( value ) : new SingletonBean( value ) );
	}
}
