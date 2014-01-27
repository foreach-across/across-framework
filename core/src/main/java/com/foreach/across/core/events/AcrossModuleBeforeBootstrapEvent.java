package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

public class AcrossModuleBeforeBootstrapEvent implements AcrossEvent
{
	private final AcrossContext context;
	private final AcrossModule module;

	public AcrossModuleBeforeBootstrapEvent( AcrossContext context, AcrossModule module ) {
		this.context = context;
		this.module = module;
	}

	public AcrossContext getContext() {
		return context;
	}

	public AcrossModule getModule() {
		return module;
	}

	/**
	 * Add any number of configurers to the module being bootstrapped.
	 *
	 * @param configurers One or more ApplicationContextConfigurer instances to add.
	 */
	public void addApplicationContextConfigurers( ApplicationContextConfigurer... configurers ) {
		for ( ApplicationContextConfigurer configurer : configurers ) {
			module.addApplicationContextConfigurer( configurer );
		}
	}
}
