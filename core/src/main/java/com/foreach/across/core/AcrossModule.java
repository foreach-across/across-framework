package com.foreach.across.core;

import com.foreach.across.core.events.AcrossEvent;
import org.springframework.context.ApplicationContext;

public abstract class AcrossModule
{
	private AcrossContext context;

	private ApplicationContext applicationContext;

	private boolean exposeBeansToParent = true;

	public AcrossContext getContext() {
		return context;
	}

	protected void setContext( AcrossContext context ) {
		this.context = context;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	void setApplicationContext( ApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
	}

	public boolean isExposeBeansToParent() {
		return exposeBeansToParent;
	}

	public void setExposeBeansToParent( boolean exposeBeansToParent ) {
		this.exposeBeansToParent = exposeBeansToParent;
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

	/**
	 * Publishes an event in the ApplicationContext of this module.  All AcrossContextEventListeners from all modules,
	 * all AcrossModuleEventListeners in this module and any other listeners in the parent ApplicationContexts will
	 * receive this event.
	 *
	 * @param event Event instance that will be published.
	 */
	public void publishEvent( AcrossEvent event ) {
		applicationContext.publishEvent( event );
	}
}
