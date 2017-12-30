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

import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.annotations.EventName;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;
import java.util.*;

public class CustomEventHandlers
{
	private Set<SimpleEvent> receivedAll = new HashSet<SimpleEvent>();
	private Set<SimpleEvent> receivedOne = new HashSet<SimpleEvent>();
	private Set<SimpleEvent> receivedTwo = new HashSet<SimpleEvent>();

	private Set<SimpleEvent> receivedTypedLongMap = new HashSet<>();
	private Set<SimpleEvent> receivedTypedIntegerList = new HashSet<>();
	private Set<SimpleEvent> receivedTypedNumberCollection = new HashSet<>();

	private Set<SingleGenericEvent> receivedSingleNumber = new HashSet<>();
	private Set<SingleGenericEvent> receivedSingleInteger = new HashSet<>();
	private Set<SingleGenericEvent> receivedSingleDecimal = new HashSet<>();

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

	public Set<SingleGenericEvent> getReceivedSingleNumber() {
		return receivedSingleNumber;
	}

	public Set<SingleGenericEvent> getReceivedSingleInteger() {
		return receivedSingleInteger;
	}

	public Set<SingleGenericEvent> getReceivedSingleDecimal() {
		return receivedSingleDecimal;
	}

	@Event
	public void allEvents( SimpleEvent event ) {
		receivedAll.add( event );
	}

	@Event
	public void namedOne( @EventName({ "one", "three" }) NamedEvent event ) {
		receivedOne.add( event );
	}

	@EventListener(condition = "#event.eventName == 'two' or #event.eventName == 'three'")
	public void namedTwo( NamedEvent event ) {
		receivedTwo.add( event );
	}

	@Event
	public void typedLongMap( GenericEvent<Long, ? extends Map> event ) {
		receivedTypedLongMap.add( event );
	}

	@EventListener
	public void typedIntegerList( GenericEvent<Integer, ? extends List<Integer>> event ) {
		receivedTypedIntegerList.add( event );
	}

	@Event
	public void typedNumberCollection( GenericEvent<? extends Number, ? extends Collection> event ) {
		receivedTypedNumberCollection.add( event );
	}

	@Event
	public void singleNumber( SingleGenericEvent<? extends Number> number ) {
		receivedSingleNumber.add( number );
	}

	@EventListener
	public void singleInteger( SingleGenericEvent<Integer> number ) {
		receivedSingleInteger.add( number );
	}

	@EventListener
	public void singleDecimal( SingleGenericEvent<BigDecimal> number ) {
		receivedSingleDecimal.add( number );
	}
}
