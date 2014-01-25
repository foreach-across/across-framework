package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.
 */
public class AcrossEventPublisher extends MBassador<AcrossEvent>
{
	@Autowired
	private AcrossContext context;

	public AcrossContext getContext() {
		return context;
	}

	public AcrossEventPublisher() {
		super( BusConfiguration.Default() );
	}
}
