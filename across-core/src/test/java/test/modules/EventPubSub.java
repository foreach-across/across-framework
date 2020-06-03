/*
 * Copyright 2019 the original author or authors
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
package test.modules;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.events.AcrossEvent;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Test bean that listens and publishes.
 *
 * @author Arne Vandamme
 * @since 2.1.1
 */
@Exposed
@RequiredArgsConstructor
public class EventPubSub
{
	private final String name;
	private final ApplicationEventPublisher eventPublisher;

	@EventListener
	public void receive( ByName byName ) {
		byName.received( this );
	}

	public ByName publish( String name ) {
		ByName event = new ByName( name );
		eventPublisher.publishEvent( event );
		return event;
	}

	@Value
	public static class ByName implements AcrossEvent
	{
		private final String name;
		private final List<String> receivedBy = new ArrayList<>();

		void received( EventPubSub listener ) {
			receivedBy.add( listener.name );
		}
	}
}
