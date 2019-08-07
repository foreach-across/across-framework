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

package com.foreach.across.core.events;

import org.springframework.context.event.EventListener;

/**
 * Event with a specific name that can be used for name based filtering of events.
 * Can be used with {@link EventListener#condition()}:.
 * <pre>
 * {@code
 *     @EventListener(condition="#event.eventName == 'my name'")
 *     public void handleEvent( NamedAcrossEvent e ) {}
 * }
 * </pre>
 */
public interface NamedAcrossEvent extends AcrossEvent
{
	/**
	 * @return Name associated with this event.
	 */
	String getEventName();
}
