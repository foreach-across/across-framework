package com.foreach.across.core.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Holds the Spring ApplicationContext information for the AcrossContext or an AcrossModule.
 */
public class AcrossApplicationContext
{
	private final AbstractApplicationContext applicationContext;
	private final AcrossApplicationContext parent;

	public AcrossApplicationContext( AbstractApplicationContext applicationContext ) {
		this( applicationContext, null );
	}

	public AcrossApplicationContext( AbstractApplicationContext applicationContext, AcrossApplicationContext parent ) {
		this.applicationContext = applicationContext;
		this.parent = parent;
	}

	public AbstractApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ConfigurableListableBeanFactory getBeanFactory() {
		return applicationContext.getBeanFactory();
	}

	public AcrossApplicationContext getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}
}
