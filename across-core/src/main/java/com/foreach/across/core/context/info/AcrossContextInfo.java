package com.foreach.across.core.context.info;

import com.foreach.across.core.AcrossContext;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * Holds the intermediate and final state of a running AcrossContext.
 */
public interface AcrossContextInfo
{
	String BEAN = "across.contextInfo";

	/**
	 * @return The original AcrossContext.
	 */
	AcrossContext getConfiguration();

	/**
	 * Gets the collection of all modules that were configured in the context,
	 * even if they were disabled.
	 *
	 * @return Collection of AcrossModuleInfo instances.
	 */
	Collection<AcrossModuleInfo> getConfiguredModules();

	/**
	 * Gets the collection of all modules that are either bootstrapped, or
	 * slated to be bootstrapped.
	 *
	 * @return Collection of AcrossModuleInfo instances.
	 */
	Collection<AcrossModuleInfo> getModules();

	/**
	 * @param moduleName Unique name of the module.
	 * @return True if the module with that name is configured on the context.
	 */
	boolean hasModule( String moduleName );

	/**
	 * @param moduleName Unique name of the module.
	 * @return AcrossModuleInfo instance or null if not present.
	 */
	AcrossModuleInfo getModuleInfo( String moduleName );

	/**
	 * @return AcrossModuleInfo instance of null if bootstrap finished.
	 */
	AcrossModuleInfo getModuleBeingBootstrapped();

	/**
	 * @return True if the AcrossContext is bootstrapped, false if bootstrap in progress.
	 */
	public boolean isBootstrapped();

	/**
	 * @return The Spring application context for this Across context.
	 */
	public ApplicationContext getApplicationContext();

}
