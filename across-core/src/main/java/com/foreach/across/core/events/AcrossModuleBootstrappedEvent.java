package com.foreach.across.core.events;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;

public class AcrossModuleBootstrappedEvent implements AcrossEvent
{
	private final AcrossModuleInfo module;

	public AcrossModuleBootstrappedEvent( AcrossModuleInfo module ) {
		this.module = module;
	}

	public AcrossContextInfo getContext() {
		return module.getContextInfo();
	}

	public AcrossModuleInfo getModule() {
		return module;
	}
}
