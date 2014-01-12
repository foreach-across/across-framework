package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

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
