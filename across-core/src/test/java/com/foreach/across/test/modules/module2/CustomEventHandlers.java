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
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.annotations.EventName;

import java.util.*;

@AcrossEventHandler
public class CustomEventHandlers
{
	private Set<SimpleEvent> receivedAll = new HashSet<SimpleEvent>();
	private Set<SimpleEvent> receivedOne = new HashSet<SimpleEvent>();
	private Set<SimpleEvent> receivedTwo = new HashSet<SimpleEvent>();

	private Set<SimpleEvent> receivedTypedLongMap = new HashSet<>();
	private Set<SimpleEvent> receivedTypedIntegerList = new HashSet<>();
	private Set<SimpleEvent> receivedTypedNumberCollection = new HashSet<>();

	public Set<SimpleEvent> getReceivedAll() {
		return receivedAll;
	}

	public Set<SimpleEvent> getReceivedOne() {
		return receivedOne;
	}

	public Set<SimpleEvent> getReceivedTwo() {
		return receivedTwo;
	}

	public Set<SimpleEvent> getReceivedTypedLongMap() {
		return receivedTypedLongMap;
	}

	public Set<SimpleEvent> getReceivedTypedIntegerList() {
		return receivedTypedIntegerList;
	}

	public Set<SimpleEvent> getReceivedTypedNumberCollection() {
		return receivedTypedNumberCollection;
	}

	@Event
	public void allEvents( SimpleEvent event ) {
		receivedAll.add( event );
	}

	@Event
	public void namedOne( @EventName({ "one", "three" }) NamedEvent event ) {
		receivedOne.add( event );
	}

	@Event
	public void namedTwo( @EventName({ "two", "three" }) NamedEvent event ) {
		receivedTwo.add( event );
	}

	@Event
	public void typedLongMap( GenericEvent<Long, Map> event ) {
		receivedTypedLongMap.add( event );
	}

	@Event
	public void typedIntegerList( GenericEvent<Integer, List> event ) {
		receivedTypedIntegerList.add( event );
	}

	@Event
	public void typedNumberCollection( GenericEvent<Number, Collection> event ) {
		receivedTypedNumberCollection.add( event );
	}
}
