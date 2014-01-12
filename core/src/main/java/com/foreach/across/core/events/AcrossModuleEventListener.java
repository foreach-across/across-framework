package com.foreach.across.core.events;

import org.springframework.context.ApplicationListener;

/**
 * Implementations will listen for events in the Spring ApplicationContext of the module itself.
 * These listeners will not listen to the parent AcrossContext and will not capture events from
 * other modules.
 *
 * Note that events launched within an AcrossModule will still be sent up to the parent contexts.
 *
 * @param <T>
 */
public interface AcrossModuleEventListener<T extends AcrossEvent> extends ApplicationListener<T>
{
}
