package com.foreach.across.core.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Holds the Spring ApplicationContext information for the AcrossContext or an AcrossModule.
 */
public class AcrossApplicationContextHolder
{
	private final AbstractApplicationContext applicationContext;
	private final AcrossApplicationContextHolder parent;

	public AcrossApplicationContextHolder( AbstractApplicationContext applicationContext ) {
		this( applicationContext, null );
	}

	public AcrossApplicationContextHolder( AbstractApplicationContext applicationContext,
	                                       AcrossApplicationContextHolder parent ) {
		this.applicationContext = applicationContext;
		this.parent = parent;
	}

	public AbstractApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ConfigurableListableBeanFactory getBeanFactory() {
		return applicationContext.getBeanFactory();
	}

	public AcrossApplicationContextHolder getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}
}
