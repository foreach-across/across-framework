package com.foreach.across.test.modules.module2;

import com.foreach.across.core.events.ParameterizedAcrossEvent;

public class GenericEvent<T, Y> extends SimpleEvent implements ParameterizedAcrossEvent
{
	private final Class<T> classOne;
	private final Class<Y> classTwo;

	public GenericEvent( Class<T> classOne, Class<Y> classTwo ) {
		this.classOne = classOne;
		this.classTwo = classTwo;
	}

	@Override
	public Class[] getGenericTypes() {
		return new Class[] { classOne, classTwo };
	}
}
