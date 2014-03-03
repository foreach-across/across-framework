package com.foreach.across.test.modules;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

@AcrossEventHandler
public class TestContextEventListener
{
	public List<TestEvent> eventsReceived = new ArrayList<TestEvent>();

	public List<TestEvent> getEventsReceived() {
		return eventsReceived;
	}

	@Handler
	public void onApplicationEvent( TestEvent event ) {
		eventsReceived.add( event );
	}
}
