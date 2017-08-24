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

import com.foreach.across.core.OrderedInModule;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.context.AcrossContextUtils;
import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.MessageBusException;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>EventBus implementation for the AcrossContext.  Allows for publishing AcrossEvent
 * instances to all registered listeners.  Also provides the necessary functionality
 * to register and unregister listeners.</p>
 * <p><strong>Note:</strong> JDK proxies are not supported, meaning the target object
 * of the proxy will be registered as event handler instead of the proxied bean.</p>
 * <p>This implementation contains some serious hacks and workarounds in order to allow listeners
 * to be ordered according to their module ordering or order-related annotations.</p>
 */
public class MBassadorEventPublisher implements AcrossEventPublisher
{
	private final MBassador<AcrossEvent> mbassador;

	private int defaultPriority = 1000;

	public MBassadorEventPublisher() {
		BusConfiguration defaultConfig = new BusConfiguration();
		defaultConfig.addFeature( Feature.SyncPubSub.Default().setSubscriptionFactory( new SubscriptionFactory() ) );
		defaultConfig.addFeature( Feature.AsynchronousHandlerInvocation.Default() );
		defaultConfig.addFeature( Feature.AsynchronousMessageDispatch.Default() );

		this.mbassador = new MBassador<>( defaultConfig );

		addErrorHandler( error -> LOG.error( "Event handling error occurred: {}", error, error.getCause() ) );

		mbassador.subscribe( this );
	}

	@Override
	public boolean unsubscribe( Object listener ) {
		Object target = AcrossContextUtils.getProxyTarget( listener );
		return target != null && mbassador.unsubscribe( target );
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

	@Event
	void handleModuleBootstrapStart( AcrossModuleBeforeBootstrapEvent beforeModuleBootstrappedEvent ) {
		defaultPriority += 1000;
	}

	@Event
	void handleContextBootstrapped( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
		defaultPriority += 1000;
	}

	private class SubscriptionFactory extends net.engio.mbassy.subscription.SubscriptionFactory
	{
		private final Field contextField, handlerField, priorityField;

		SubscriptionFactory() {
			contextField = ReflectionUtils.findField( Subscription.class, "context" );
			contextField.setAccessible( true );

			handlerField = ReflectionUtils.findField( SubscriptionContext.class, "handlerMetadata" );
			handlerField.setAccessible( true );

			priorityField = ReflectionUtils.findField( MessageHandler.class, "priority" );
			priorityField.setAccessible( true );
		}

		@Override
		public Subscription createSubscription( BusRuntime runtime, MessageHandler handlerMetadata ) throws MessageBusException {
			Subscription subscription = super.createSubscription( runtime, handlerMetadata );

			// hack subscription priority if no explicit priority set
			MessageHandler messageHandler = (MessageHandler) ReflectionUtils.getField( handlerField, ReflectionUtils.getField( contextField, subscription ) );
			int currentPriority = (int) ReflectionUtils.getField( priorityField, messageHandler );
			if ( currentPriority == 0 ) {
				ReflectionUtils.setField( priorityField, messageHandler, calculatePriority( messageHandler.getHandler() ) );
			}

			return subscription;
		}

		private int calculatePriority( Method method ) {
			Order order = AnnotationUtils.findAnnotation( method, Order.class );
			OrderInModule orderInModule = null;

			if ( order == null ) {
				orderInModule = AnnotationUtils.findAnnotation( method, OrderInModule.class );

				if ( orderInModule == null ) {
					order = AnnotationUtils.findAnnotation( method.getDeclaringClass(), Order.class );

					if ( order == null ) {
						orderInModule = AnnotationUtils.findAnnotation( method.getDeclaringClass(), OrderInModule.class );
					}
				}
			}

			if ( order == null && orderInModule == null ) {
				if ( Ordered.class.isAssignableFrom( method.getDeclaringClass() ) ) {
					LOG.warn( "Listener implements Ordered interface which is not supported for event handler ordering." );
				}
				else if ( OrderedInModule.class.isAssignableFrom( method.getDeclaringClass() ) ) {
					LOG.warn( "Listener implements OrderedInModule interface which is not supported for event handler ordering." );
				}
			}
			else {
				if ( order != null ) {
					return -order.value();
				}
				else {
					return -( defaultPriority + orderInModule.value() );
				}
			}

			return -( defaultPriority + 100 );
		}
	}
}
