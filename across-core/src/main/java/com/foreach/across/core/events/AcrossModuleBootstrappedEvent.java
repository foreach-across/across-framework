package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;

public class AcrossModuleBootstrappedEvent implements AcrossEvent
{
	private final AcrossContext context;
	private final AcrossModule module;

	public AcrossModuleBootstrappedEvent( AcrossContext context, AcrossModule module ) {
		this.context = context;
		this.module = module;
	}

	public AcrossContext getContext() {
		return context;
	}

	public AcrossModule getModule() {
		return module;
	}
}
