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

import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.</p>
 */
public interface AcrossEventPublisher
{
	Logger LOG = LoggerFactory.getLogger( AcrossEventPublisher.class );

	/**
	 * Remove a listener instance.  Safe to call even if the instance was never registered as a listener.
	 *
	 * @param listener instance to remove
	 * @return true if the listener was registered
	 */
	boolean unsubscribe( Object listener );

	/**
	 * Add an object as an event listener, scans it for {@link com.foreach.across.core.annotations.Event}
	 * annotated methods. Safe to call even if the instance has no such methods.
	 *
	 * @param listener instance
	 */
	void subscribe( Object listener );

	/**
	 * Publish an event.
	 *
	 * @param event to publish
	 */
	void publish( AcrossEvent event );

	/**
	 * Add an error handler that will be called whenever an event handler has thrown an error while
	 * handling an event.  Note that if this happens, all other event handlers will still be called.
	 *
	 * @param errorHandler instance
	 */
	void addErrorHandler( IPublicationErrorHandler errorHandler );
}
