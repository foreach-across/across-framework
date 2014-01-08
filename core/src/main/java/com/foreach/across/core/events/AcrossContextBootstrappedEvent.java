package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

public class AcrossContextBootstrappedEvent extends ApplicationEvent
{
	private final AcrossContext context;
	private final Collection<AcrossModule> modules;

	public AcrossContextBootstrappedEvent( AcrossContext context, Collection<AcrossModule> modules ) {
		super( context );
		this.context = context;
		this.modules = modules;
	}

	public AcrossContext getContext() {
		return context;
	}

	public Collection<AcrossModule> getModules() {
		return modules;
	}
}
