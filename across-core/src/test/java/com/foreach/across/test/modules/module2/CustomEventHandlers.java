/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.test.modules.module2;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.EventName;
import com.foreach.across.core.events.EventNameFilter;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;

import java.util.HashSet;
import java.util.Set;

@AcrossEventHandler
public class CustomEventHandlers
{

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
	public void allEvents( SimpleEvent event ) {
		receivedAll.add( event );
	}

	@Handler(filters = @Filter(EventNameFilter.class))
	public void namedOne( @EventName({ "one", "three" }) NamedEvent event ) {
		receivedOne.add( event );
	}

	@Handler(filters = @Filter(EventNameFilter.class))
	public void namedTwo( @EventName({ "two", "three" }) NamedEvent event ) {
		receivedTwo.add( event );
	}
}
