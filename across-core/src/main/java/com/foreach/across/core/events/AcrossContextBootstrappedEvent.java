package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;

public class AcrossContextBootstrappedEvent implements AcrossEvent
{
	private final AcrossContext context;

	public AcrossContextBootstrappedEvent( AcrossContext context ) {
		this.context = context;
	}

	public AcrossContext getContext() {
		return context;
	}
}
