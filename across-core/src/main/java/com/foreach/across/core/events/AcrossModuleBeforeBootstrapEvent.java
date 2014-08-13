package com.foreach.across.core.events;

import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;

/**
 * Event that is sent right before the module is bootstrapped.
 * The ModuleBootstrapConfig can still be modified at this point.
 *
 * @see com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig
 */
public class AcrossModuleBeforeBootstrapEvent implements AcrossEvent
{
	private final AcrossContextInfo context;
	private final AcrossModuleInfo module;

	public AcrossModuleBeforeBootstrapEvent( AcrossContextInfo context, AcrossModuleInfo module ) {
		this.context = context;
		this.module = module;
	}

	public AcrossContextInfo getContext() {
		return context;
	}

	public AcrossModuleInfo getModule() {
		return module;
	}

	public ModuleBootstrapConfig getBootstrapConfig() {
		return module.getBootstrapConfiguration();
	}

	/**
	 * Add any number of configurers to the module being bootstrapped.
	 *
	 * @param configurers One or more ApplicationContextConfigurer instances to add.
	 */
	public void addApplicationContextConfigurers( ApplicationContextConfigurer... configurers ) {
		getBootstrapConfig().addApplicationContextConfigurer( configurers );
	}
}
