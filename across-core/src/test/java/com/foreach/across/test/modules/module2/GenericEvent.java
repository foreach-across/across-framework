package com.foreach.across.test.modules.module2;

import com.foreach.across.core.events.ParameterizedAcrossEvent;
import org.springframework.core.ResolvableType;

public class GenericEvent<T, Y> extends SimpleEvent implements ParameterizedAcrossEvent
{
	private final ResolvableType[] genericTypes;

	public GenericEvent( Class<T> classOne, Class classTwo ) {
		genericTypes = new ResolvableType[] {
				ResolvableType.forClass( classOne ),
				ResolvableType.forClass( classTwo )
		};
	}

	public GenericEvent( Class<T> classOne, ResolvableType resolvableType ) {
		genericTypes = new ResolvableType[] {
				ResolvableType.forClass( classOne ),
				resolvableType
		};
	}

	@Override
	public ResolvableType[] getEventGenericTypes() {
		return genericTypes;
	}
}
