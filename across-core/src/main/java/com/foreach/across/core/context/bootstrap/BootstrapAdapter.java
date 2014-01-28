package com.foreach.across.core.context.bootstrap;

/**
 * Modules implementing this interface can modify the BootstrapFactories.
 */
public interface BootstrapAdapter
{
	/**
	 * Customize the AcrossBootstrapper involved.
	 *
	 * @param bootstrapper AcrossBootstrapper instance.
	 */
	void customizeBootstrapper( AcrossBootstrapper bootstrapper );
}
