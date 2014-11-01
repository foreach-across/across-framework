package com.foreach.across.core.events;

import org.springframework.core.ResolvableType;

/**
 * Event type that has one or more parameterized (generic) types.
 */
public interface ParameterizedAcrossEvent extends AcrossEvent
{
	/**
	 * The event generic types should be returned as ResolvableTypes so in turn they
	 * can contain generic parameters.
	 *
	 * @return The array of generic parameter types of this event.
	 */
	ResolvableType[] getEventGenericTypes();
}
