package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AcrossEventPublisher extends MBassador<Object>
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
