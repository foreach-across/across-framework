package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;

import java.util.HashMap;
import java.util.Map;

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

	private Map<String, Object> additionalSingletons = new HashMap<String, Object>();

}
