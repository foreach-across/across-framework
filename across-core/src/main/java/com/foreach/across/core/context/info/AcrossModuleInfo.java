package com.foreach.across.core.context.info;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossEntity;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

public interface AcrossModuleInfo extends AcrossEntity
{
	/**
	 * @return The info object of the AcrossContext this module belongs to.
	 */
	public AcrossContextInfo getContextInfo();

	/**
	 * @return The index of the module in the context bootstrap.
	 */
	int getIndex();

	/**
	 * @return Name of the AcrossModule.
	 */
	public String getName();

	/**
	 * @return Description of the AcrossModule.
	 */
	public String getDescription();

	/**
	 * @return AcrossModule instance.
	 */
	public AcrossModule getModule();

	/**
	 * @return Collection of required module dependencies.
	 */
	public Collection<AcrossModuleInfo> getRequiredDependencies();

	/**
	 * @return Collection of optional module dependencies.
	 */
	public Collection<AcrossModuleInfo> getOptionalDependencies();

	/**
	 * @return True if the module was enabled and should be bootstrapped.
	 */
	public boolean isEnabled();

	/**
	 * @return True if the module has been bootstrapped.
	 */
	public boolean isBootstrapped();

	/**
	 	 * @return Actual status of the module boostrap.
	 */
	public ModuleBootstrapStatus getBootstrapStatus();

	/**
	 * @return Configuration object used for bootstrapping this module.  Null if module was disabled.
	 */
	public ModuleBootstrapConfig getBootstrapConfiguration();

	/**
	 * @return The Spring application context for this module.
	 */
	public ApplicationContext getApplicationContext();

	/**
	 * @return The specific role of the module in the context.
	 */
	public AcrossModuleRole getModuleRole();
}
