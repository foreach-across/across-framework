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

import com.foreach.across.core.context.AcrossContextUtils;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import javax.annotation.PreDestroy;

/**
 * <p>EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.</p>
 * <p><strong>Note:</strong> JDK proxies are not supported, meaning the target object
 * of the proxy will be registered as event handler instead of the proxied bean.</p>
 */
public class MBassadorEventPublisher implements AcrossEventPublisher
{
	private final MBassador<AcrossEvent> mbassador;

	public MBassadorEventPublisher() {
		BusConfiguration defaultConfig = new BusConfiguration();
		defaultConfig.addFeature( Feature.SyncPubSub.Default() );
		defaultConfig.addFeature( Feature.AsynchronousHandlerInvocation.Default() );
		defaultConfig.addFeature( Feature.AsynchronousMessageDispatch.Default() );

		this.mbassador = new MBassador<>( defaultConfig );

		addErrorHandler( error -> LOG.error( "Event handling error occurred: {}", error, error.getCause() ) );
	}

	@Override
	public boolean unsubscribe( Object listener ) {
		return mbassador.unsubscribe( AcrossContextUtils.getProxyTarget( listener ) );
	}

	@Override
	public void subscribe( Object listener ) {
		Object target = AcrossContextUtils.getProxyTarget( listener );
		if ( target != null ) {
			mbassador.subscribe( target );
		}
	}

	@Override
	public void publish( AcrossEvent event ) {
		mbassador.publish( event );
	}

	@Override
	public void publishAsync( AcrossEvent event ) {
		mbassador.publishAsync( event );
	}

	@Override
	public void addErrorHandler( IPublicationErrorHandler errorHandler ) {
		mbassador.addErrorHandler( errorHandler );
	}

	@PreDestroy
	public void shutdown() {
		mbassador.shutdown();
	}
}
