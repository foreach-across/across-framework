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
