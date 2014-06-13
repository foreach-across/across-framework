package com.foreach.across.core.events;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;

import java.util.Collection;

public class AcrossContextBootstrappedEvent implements AcrossEvent
{
	private final AcrossContextInfo contextInfo;

	public AcrossContextBootstrappedEvent( AcrossContextInfo contextInfo ) {
		this.contextInfo = contextInfo;
	}

	public AcrossContextInfo getContext() {
		return contextInfo;
	}

	/**
	 * @return The actual collection of all modules that have bootstrapped.
	 */
	public Collection<AcrossModuleInfo> getModules() {
		return contextInfo.getModules();
	}
}
