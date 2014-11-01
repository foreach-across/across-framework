package com.foreach.across.core.events;

/**
 * Event type that has one or more parameterized (generic) types.
 */
public interface ParameterizedAcrossEvent extends AcrossEvent
{
	/**
	 * @return The array of generic parameter types of this event.
	 */
	Class[] getGenericTypes();
}
