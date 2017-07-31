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
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.events.*;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestMBassadorEventPublisher
{
	private AcrossEventPublisher eventPublisher;

	private List<Object> handled = new ArrayList<>();

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

	@Test
	public void eventsArePublishedInOrder() {
		Unordered unordered = new Unordered();
		Ordered ordered = new Ordered();
		MethodOrdered methodOrdered = new MethodOrdered();

		eventPublisher.subscribe( unordered );
		eventPublisher.subscribe( ordered );
		eventPublisher.subscribe( methodOrdered );

		eventPublisher.publish( mock( AcrossEvent.class ) );

		assertEquals( Arrays.asList( "method-ordered-one", ordered, "method-ordered-two", unordered ), handled );
	}

	@Test
	public void moduleAndBootstrapInfluenceOrder() {
		Ordered ordered = new Ordered();
		// Use concrete classes - dynamic ordering is not supported
		Unordered one = new Unordered();
		Unordered two = new Unordered2();
		Unordered three = new Unordered3();
		Unordered four = new Unordered4();

		eventPublisher.subscribe( one );
		eventPublisher.publish( mock( AcrossModuleBeforeBootstrapEvent.class ) );
		eventPublisher.subscribe( two );
		eventPublisher.publish( mock( AcrossModuleBeforeBootstrapEvent.class ) );
		eventPublisher.subscribe( three );
		eventPublisher.publish( mock( AcrossContextBootstrappedEvent.class ) );
		eventPublisher.subscribe( four );
		eventPublisher.subscribe( ordered );

		handled.clear();
		eventPublisher.publish( mock( AcrossEvent.class ) );

		assertEquals( Arrays.asList( ordered, one, two, three, four ), handled );
	}

	@Test
	public void subscribeWithLazyProxyDoesNotNullPointerAndDoesNotEagerlyInstantiate() throws Exception {
		AtomicReference<EventHandlerProxy> lazyEventHandlerProxy = new AtomicReference<>();
		eventPublisher.subscribe( createLazyProxy( lazyEventHandlerProxy, EventHandlerProxy.class ) );
		EventHandler lazyEventHandler = new EventHandler();
		lazyEventHandlerProxy.set( lazyEventHandler );

		AcrossEvent eventOne = mock( AcrossEvent.class );
		eventPublisher.publish( eventOne );
		assertEquals( 0, lazyEventHandler.received.size() );
	}

	private <T> T createLazyProxy( AtomicReference<T> reference, Class<T> type ) {
		// As seen in SimpleBatchConfiguration
		ProxyFactory factory = new ProxyFactory();
		factory.setTargetSource( new ReferenceTargetSource<T>( reference ) );
		factory.addAdvice( new PassthruAdvice() );
		factory.setInterfaces( new Class<?>[] { type } );
		@SuppressWarnings("unchecked")
		T proxy = (T) factory.getProxy();
		return proxy;
	}

	protected interface EventHandlerProxy
	{
	}

	private static class EventHandler implements EventHandlerProxy
	{
		public final List<AcrossEvent> received = new ArrayList<>();

		@Event
		public void handle( AcrossEvent event ) {
			received.add( event );
		}
	}

	private class PassthruAdvice implements MethodInterceptor
	{
		@Override
		public Object invoke( MethodInvocation invocation ) throws Throwable {
			return invocation.proceed();
		}
	}

	private class ReferenceTargetSource<T> extends AbstractLazyCreationTargetSource
	{
		private AtomicReference<T> reference;

		public ReferenceTargetSource( AtomicReference<T> reference ) {
			this.reference = reference;
		}

		@Override
		protected Object createObject() throws Exception {
			fail( "A lazy proxy should not be instantiated eagerly" );
			return reference.get();
		}
	}

	private class Unordered
	{
		@Handler(filters = { @Filter(ParameterizedAcrossEventFilter.class), @Filter(EventNameFilter.class) })
		public void handlerUnordered( AcrossEvent event ) {
			handled.add( this );
		}
	}

	private class Unordered2 extends Unordered
	{
	}

	private class Unordered3 extends Unordered
	{
	}

	private class Unordered4 extends Unordered
	{
	}

	@Order(1)
	private class Ordered
	{
		@Handler(filters = { @Filter(ParameterizedAcrossEventFilter.class), @Filter(EventNameFilter.class) })
		public void handlerOrdered( AcrossEvent event ) {
			handled.add( this );
		}
	}

	@Order(2)
	private class MethodOrdered
	{
		@Order(-1)
		@Handler(filters = { @Filter(ParameterizedAcrossEventFilter.class), @Filter(EventNameFilter.class) })
		public void methodGlobalOrdered( AcrossEvent event ) {
			handled.add( "method-ordered-one" );
		}

		@OrderInModule(1)
		@Handler(filters = { @Filter(ParameterizedAcrossEventFilter.class), @Filter(EventNameFilter.class) })
		public void methodModuleOrdered( AcrossEvent event ) {
			handled.add( "method-ordered-two" );
		}
	}
}
