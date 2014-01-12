package com.foreach.across.core.events;

import org.springframework.context.ApplicationListener;

/**
 * Implementations will hook up to the ApplicationContext registered, and will listen to all events
 * published in any of the modules in the context.
 *
 * Usually events will be launched in the AcrossContext instead of in the AcrossModule.
 *
 * @param <T>
 */
public interface AcrossContextEventListener<T extends AcrossEvent> extends ApplicationListener<T>
{
}
