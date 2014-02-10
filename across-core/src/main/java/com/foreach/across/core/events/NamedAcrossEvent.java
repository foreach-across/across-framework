package com.foreach.across.core.events;

/**
 *
 */
public interface NamedAcrossEvent extends AcrossEvent {

    /**
     * @return Name associated with this event.
     */
    String getEventName();
}
