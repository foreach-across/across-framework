package com.foreach.across.core.events;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.AcrossContextUtils;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;

/**
 * <p>EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.</p>
 * <p/>
 * <p><strong>Note:</strong> JDK proxies are not supported, meaning the target object
 * of the proxy will be registered as event handler instead of the proxied bean.</p>
 */
public class MBassadorEventPublisher extends MBassador<AcrossEvent> implements AcrossEventPublisher {
    public MBassadorEventPublisher() {
        super(BusConfiguration.Default());
    }

    public boolean unsubscribe(Object listener) {
        return super.unsubscribe(AcrossContextUtils.getProxyTarget(listener));
    }

    public void subscribe(Object listener) {
        super.subscribe(AcrossContextUtils.getProxyTarget(listener));
    }
}
