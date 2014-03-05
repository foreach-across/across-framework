package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

/**
 * Event that is sent right before the module is bootstrapped.
 * The ModuleBootstrapConfig can still be modified at this point.
 *
 * @see com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig
 */
public class AcrossModuleBeforeBootstrapEvent implements AcrossEvent
{
	private final AcrossContext context;
	private final ModuleBootstrapConfig bootstrapConfig;

	public AcrossModuleBeforeBootstrapEvent( AcrossContext context, ModuleBootstrapConfig bootstrapConfig ) {
		this.context = context;
		this.bootstrapConfig = bootstrapConfig;
	}

	public AcrossContext getContext() {
		return context;
	}

	public AcrossModule getModule() {
		return bootstrapConfig.getModule();
	}

	public ModuleBootstrapConfig getBootstrapConfig() {
		return bootstrapConfig;
	}

	/**
	 * Add any number of configurers to the module being bootstrapped.
	 *
	 * @param configurers One or more ApplicationContextConfigurer instances to add.
	 */
	public void addApplicationContextConfigurers( ApplicationContextConfigurer... configurers ) {
		bootstrapConfig.addApplicationContextConfigurer( configurers );
	}
}
