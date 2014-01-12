package com.foreach.across.test.modules;

import com.foreach.across.core.events.AcrossModuleEventListener;

import java.util.ArrayList;
import java.util.List;

public class TestModuleEventListener implements AcrossModuleEventListener<TestEvent>
{
	public List<TestEvent> eventsReceived = new ArrayList<TestEvent>();

	public List<TestEvent> getEventsReceived() {
		return eventsReceived;
	}

	public void onApplicationEvent( TestEvent event ) {
		eventsReceived.add( event );
	}
}