package com.foreach.across.core.context.info;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossEntity;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * Holds the intermediate and final state of a running AcrossContext.
 */
public interface AcrossContextInfo extends AcrossEntity
{
	String BEAN = "across.contextInfo";

	/**
	 * @return The unique id of the AcrossContext.
	 */
	String getId();

	/**
	 * @return The original AcrossContext.
	 */
	AcrossContext getContext();

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
	boolean isBootstrapped();

	/**
	 * @return The Spring application context for this Across context.
	 */
	ApplicationContext getApplicationContext();

	/**
	 * @return Configuration object used for bootstrapping the AcrossContext.
	 */
	AcrossBootstrapConfig getBootstrapConfiguration();

	/**
	 * @param moduleName Unique name of the module.
	 * @return Index of the module in the context bootstrap, max integer if not found.
	 */
	int getModuleIndex( String moduleName );

	/**
	 * @param module AcrossModule instance to lookup the index for.
	 * @return Index of the module in the context bootstrap, max integer if not found.
	 */
	int getModuleIndex( AcrossModule module );

	/**
	 * @param moduleInfo AcrossModuleInfo instance to lookup the index for.
	 * @return Index of the module in the context bootstrap, max integer if not found.
	 */
	int getModuleIndex( AcrossModuleInfo moduleInfo );

	/**
	 * Searches the AcrossContext for beans of the given type.  Depending on the scanModules boolean, this
	 * will scan the base context and its parent, or all modules separately (including non-exposed beans).
	 * <p/>
	 * All beans will be sorted according to the Order, module index and OrderInModule values.
	 *
	 * @param requiredType Type the bean should match.
	 * @param scanModules  True if the individual AcrossModules should be scanned.
	 * @param <T>          Type of the matching beans.
	 * @see com.foreach.across.core.context.ModuleBeanOrderComparator
	 * @see com.foreach.across.core.OrderedInModule
	 * @see org.springframework.core.Ordered
	 */
	<T> Collection<T> getBeansOfType( Class<T> requiredType, boolean scanModules );
}
