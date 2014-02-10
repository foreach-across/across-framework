package com.foreach.across.test.modules.module2;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.EventName;
import com.foreach.across.core.events.EventNameFilter;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;

import java.util.HashSet;
import java.util.Set;

@AcrossEventHandler
public class CustomEventHandlers {

    private Set<SimpleEvent> receivedAll = new HashSet<SimpleEvent>();
    private Set<SimpleEvent> receivedOne = new HashSet<SimpleEvent>();
    private Set<SimpleEvent> receivedTwo = new HashSet<SimpleEvent>();

    public Set<SimpleEvent> getReceivedAll() {
        return receivedAll;
    }

    public Set<SimpleEvent> getReceivedOne() {
        return receivedOne;
    }

    public Set<SimpleEvent> getReceivedTwo() {
        return receivedTwo;
    }

    @Handler
    public void allEvents(SimpleEvent event) {
        receivedAll.add(event);
    }

    @Handler(filters = @Filter(EventNameFilter.class))
    public void namedOne(@EventName({"one", "three"}) NamedEvent event) {
        receivedOne.add(event);
    }

    @Handler(filters = @Filter(EventNameFilter.class))
    public void namedTwo(@EventName({"two", "three"}) NamedEvent event) {
        receivedTwo.add(event);
    }
}
