package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.</p>
 *
 * <p><strong>Note:</strong> JDK proxies are not supported, meaning the target object
 * of the proxy will be registered as event handler instead of the proxied bean.</p>
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

	@Override
	public boolean unsubscribe( Object listener ) {
		return super.unsubscribe( AcrossContextUtils.getProxyTarget( listener ) );
	}

	@Override
	public void subscribe( Object listener ) {
		super.subscribe( AcrossContextUtils.getProxyTarget( listener ) );
	}
}
