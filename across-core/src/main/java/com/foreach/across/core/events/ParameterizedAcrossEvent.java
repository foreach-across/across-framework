package com.foreach.across.core.events;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * Event type that has one or more parameterized (generic) types.
 *
 * @deprecated implement {@link ResolvableTypeProvider} directly
 */
@Deprecated
public interface ParameterizedAcrossEvent extends AcrossEvent, ResolvableTypeProvider
{
	/**
	 * The event generic types should be returned as ResolvableTypes so in turn they
	 * can contain generic parameters.
	 *
	 * @return The array of generic parameter types of this event.
	 */
	@Deprecated
	ResolvableType[] getEventGenericTypes();

	@Override
	default ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics( getClass(), getEventGenericTypes() );
	}
}
