package com.foreach.across.core.events;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;
import org.springframework.core.ResolvableType;

/**
 * Filters event based on one or more parameter types.  The events must implement
 * {@link com.foreach.across.core.events.ParameterizedAcrossEvent} and provide the array
 * of parameter types in the declaration order.
 *
 * @see com.foreach.across.core.events.ParameterizedAcrossEvent
 * @see com.foreach.across.core.events.NamedAcrossEvent
 */
public class ParameterizedAcrossEventFilter implements IMessageFilter<AcrossEvent>
{
	@Override
	public boolean accepts( AcrossEvent message, MessageHandler metadata ) {
		if ( message instanceof ParameterizedAcrossEvent ) {
			ResolvableType[] types = ( (ParameterizedAcrossEvent) message ).getEventGenericTypes();
			ResolvableType eventType = ResolvableType.forMethodParameter( metadata.getHandler(), 0 );

			ResolvableType[] generics = eventType.getGenerics();

			if ( generics.length > 0 && types.length != generics.length ) {
				return false;
			}

			for ( int i = 0; i < generics.length; i++ ) {
				ResolvableType genericType = generics[i];

				if ( !genericType.isAssignableFrom( types[i] ) ) {
					return false;
				}
			}
		}

		return true;
	}
}
