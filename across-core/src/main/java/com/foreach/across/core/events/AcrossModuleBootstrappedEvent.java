package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;

public class AcrossModuleBootstrappedEvent implements AcrossEvent
{
	private final AcrossModuleInfo module;

	public AcrossModuleBootstrappedEvent( AcrossModuleInfo module ) {
		this.module = module;
	}

	public AcrossContextInfo getContext() {
		return module.getContext();
	}

	public AcrossModuleInfo getModule() {
		return module;
	}
}
