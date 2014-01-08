package com.foreach.across.core;

import org.springframework.context.ApplicationContext;

public abstract class AcrossModule
{
	private AcrossContext context;

	private ApplicationContext applicationContext;

	public AcrossContext getContext() {
		return context;
	}

	void setContext( AcrossContext context ) {
		this.context = context;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	void setApplicationContext( ApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return Array containing the installer classes in the order of which they should be run.
	 */
	public Object[] getInstallers() {
		return new Object[0];
	}

	/**
	 * @return Array of packages that should be scanned for components.
	 */
	public String[] getComponentScanPackages() {
		return new String[] { getClass().getPackage().getName() };
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	public abstract String getName();

	/**
	 * @return Description of the content of this module.
	 */
	public abstract String getDescription();

	/**
	 * Called after all modules have been installed and - depending on the registration order in the context -
	 * previous modules have been bootstrapped already.
	 */
	public void bootstrap() {
	}

	/**
	 * Called in case of a context shutdown.  Modules registered after this one in the context will have
	 * been shutdown already.
	 */
	public void shutdown() {
	}
}
