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
package com.foreach.across.test.events;

import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.events.AcrossEvent;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.MBassadorEventPublisher;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestMBassadorEventPublisher
{
	private AcrossEventPublisher eventPublisher;

	@Before
	public void setUp() {
		eventPublisher = new MBassadorEventPublisher();
	}

	@Test
	public void subscribeAndUnsubscribe() {
		EventHandler one = new EventHandler();
		EventHandler two = new EventHandler();

		eventPublisher.subscribe( one );
		eventPublisher.subscribe( two );

		AcrossEvent eventOne = mock( AcrossEvent.class );
		eventPublisher.publish( eventOne );

		assertEquals( Collections.singletonList( eventOne ), one.received );
		assertEquals( Collections.singletonList( eventOne ), two.received );

		AcrossEvent eventTwo = mock( AcrossEvent.class );
		eventPublisher.unsubscribe( two );
		eventPublisher.publish( eventTwo );

		assertEquals( Arrays.asList( eventOne, eventTwo ), one.received );
		assertEquals( Collections.singletonList( eventOne ), two.received );
	}

	private static class EventHandler
	{
		public final List<AcrossEvent> received = new ArrayList<>();

		@Event
		public void handle( AcrossEvent event ) {
			received.add( event );
		}
	}

}
