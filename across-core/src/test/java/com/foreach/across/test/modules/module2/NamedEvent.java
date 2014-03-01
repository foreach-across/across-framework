package com.foreach.across.test.modules.module2;

import com.foreach.across.core.events.NamedAcrossEvent;

public class NamedEvent extends SimpleEvent implements NamedAcrossEvent
{
	private final String name;

	public NamedEvent( String name ) {
		this.name = name;
	}

	public String getEventName() {
		return name;
	}
}
