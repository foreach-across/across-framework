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

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <p>EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.</p>
 */
public interface AcrossEventPublisher
{
	Logger LOG = LoggerFactory.getLogger( AcrossEventPublisher.class );

	boolean unsubscribe( Object listener );

	void subscribe( Object listener );

	MessagePublication publishAsync( AcrossEvent message );

	MessagePublication publishAsync( AcrossEvent message, long timeout, TimeUnit unit );

	void publish( AcrossEvent message );

	SyncAsyncPostCommand<AcrossEvent> post( AcrossEvent message );

	void shutdown();

	boolean isListener( Object listener );
}
