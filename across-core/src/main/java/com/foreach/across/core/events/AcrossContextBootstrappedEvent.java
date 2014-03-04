package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;

import java.util.Collection;

public class AcrossContextBootstrappedEvent implements AcrossEvent
{
	private final AcrossContext context;
	private final Collection<AcrossModule> modules;

	public AcrossContextBootstrappedEvent( AcrossContext context, Collection<AcrossModule> modules ) {
		this.context = context;
		this.modules = modules;
	}

	public AcrossContext getContext() {
		return context;
	}

	/**
	 * @return The actual collection of all modules that have bootstrapped.
	 */
	public Collection<AcrossModule> getModules() {
		return modules;
	}
}
