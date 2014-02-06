package com.foreach.across.test.modules;

import com.foreach.across.core.annotations.AcrossEventHandler;
import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;
import java.util.List;

@AcrossEventHandler
public class TestModuleEventListener
{
	private List<TestEvent> eventsReceived = new ArrayList<TestEvent>();

	public List<TestEvent> getEventsReceived() {
		return eventsReceived;
	}

	@Handler
	public void receiveEvent( TestEvent event ) {
		eventsReceived.add( event );
	}
}