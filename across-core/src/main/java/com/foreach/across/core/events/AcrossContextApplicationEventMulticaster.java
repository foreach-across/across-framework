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

import com.foreach.across.core.annotations.EventName;
import com.foreach.across.core.context.support.AcrossOrderSpecifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom implementation of {@link ApplicationEventMulticaster} for use in a single {@link com.foreach.across.core.AcrossContext}.
 * Should only be used in conjunction with an {@link com.foreach.across.core.context.AcrossApplicationContext} and events should be
 * published using the {@link org.springframework.context.ApplicationEventPublisher} and never the {@link ApplicationEventMulticaster} directly.
 * Modules should create a separate multicaster using {@link #createModuleMulticaster(Integer, BeanFactory)}.
 * <p/>
 * This multicaster will ensure that any event published will get routed through all module registered listeners.
 * It expects that typed events sent by {@link #multicastEvent(ApplicationEvent, ResolvableType)} are only published on the root
 * {@link AcrossContextApplicationEventMulticaster} and never one of the module multicasters. Since a module will always be attached
 * to the context and an event being published is always sent up to the parent.
 * <p/>
 * Any {@link EventListener} method will be adjusted to receive a default Across order.
 * When publishing an event the listeners added directly on the context will be be merged with all listeners registered on the modules,
 * and sorted again. This ensures event listeners will be executed in the expected Across module ordering.
 * <p/>
 * This implementation also supports the old {@link EventName} annotation and converts it a to a condition on {@link EventListener}.
 * <p/>
 * NOTE: the current implementation has reduced performance compared to a single multicaster as there is no overall caching.
 *
 * @author Arne Vandamme
 * @see NonExposedEventListenerMethodProcessor
 * @since 3.0.0
 */
public final class AcrossContextApplicationEventMulticaster extends SimpleApplicationEventMulticaster
{
	private final List<ModuleApplicationEventMulticaster> moduleMulticasters = new ArrayList<>();

	private static final Field METHOD_FIELD, CONDITION_FIELD, ORDER_FIELD;

	static {
		METHOD_FIELD = ReflectionUtils.findField( ApplicationListenerMethodAdapter.class, "method" );
		METHOD_FIELD.setAccessible( true );

		CONDITION_FIELD = ReflectionUtils.findField( ApplicationListenerMethodAdapter.class, "condition" );
		CONDITION_FIELD.setAccessible( true );

		ORDER_FIELD = ReflectionUtils.findField( ApplicationListenerMethodAdapter.class, "order" );
		ORDER_FIELD.setAccessible( true );
	}

	public AcrossContextApplicationEventMulticaster( BeanFactory beanFactory ) {
		super( beanFactory );
	}

	@Override
	public void addApplicationListener( ApplicationListener<?> listener ) {
		if ( listener instanceof ApplicationListenerMethodAdapter ) {
			adjustMethodEventListener( (ApplicationListenerMethodAdapter) listener, null );
		}
		super.addApplicationListener( listener );
	}

	@Override
	protected Collection<ApplicationListener<?>> getApplicationListeners( ApplicationEvent event, ResolvableType eventType ) {
		if ( !moduleMulticasters.isEmpty() ) {
			LinkedList<ApplicationListener<?>> allListeners = new LinkedList<>( super.getApplicationListeners( event, eventType ) );
			moduleMulticasters.forEach(
					multicaster -> multicaster.getApplicationListeners( event, eventType )
					                          .stream()
					                          .filter( l -> !allListeners.contains( l ) )
					                          .forEach( allListeners::add )
			);
			AnnotationAwareOrderComparator.sort( allListeners );
			return allListeners;
		}

		return super.getApplicationListeners( event, eventType );
	}

	/**
	 * Create a new multicaster for an Across module.
	 *
	 * @param moduleIndex index of the module
	 * @param beanFactory bean factory of the module
	 * @return multicaster
	 */
	public ApplicationEventMulticaster createModuleMulticaster( Integer moduleIndex, BeanFactory beanFactory ) {
		ModuleApplicationEventMulticaster moduleApplicationEventMulticaster = new ModuleApplicationEventMulticaster( beanFactory, moduleIndex, this );
		moduleMulticasters.add( moduleApplicationEventMulticaster );
		return moduleApplicationEventMulticaster;
	}

	/**
	 * Adjust a method adapter: apply module ordering and convert the old {@link com.foreach.across.core.annotations.EventName} annotations
	 * into a SpEL condition.
	 *
	 * @param listenerMethodAdapter event listener
	 * @param moduleIndex           to use
	 */
	private void adjustMethodEventListener( ApplicationListenerMethodAdapter listenerMethodAdapter, Integer moduleIndex ) {
		Method method = (Method) ReflectionUtils.getField( METHOD_FIELD, listenerMethodAdapter );

		// calculate the order
		int order = AcrossOrderSpecifier.forSources( Arrays.asList( method, method.getDeclaringClass() ) )
		                                .moduleIndex( moduleIndex )
		                                .build()
		                                .toPriority();
		ReflectionUtils.setField( ORDER_FIELD, listenerMethodAdapter, order );

		// convert the old EventName annotation to a custom condition
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		if ( parameterAnnotations.length > 0 ) {
			for ( Annotation annotation : parameterAnnotations[0] ) {
				if ( annotation instanceof EventName ) {
					String condition = StringUtils.defaultString( (String) ReflectionUtils.getField( CONDITION_FIELD, listenerMethodAdapter ) );

					EventName eventNames = (EventName) annotation;
					String namesCondition = "(" + Stream.of( eventNames.value() )
					                                    .map( n -> "#event.eventName == '" + n + "'" )
					                                    .collect( Collectors.joining( " or " ) ) + ")";
					condition = condition.isEmpty() ? namesCondition : "(" + condition + ") and " + namesCondition;
					ReflectionUtils.setField( CONDITION_FIELD, listenerMethodAdapter, condition );
				}
			}
		}
	}

	private static class ModuleApplicationEventMulticaster extends SimpleApplicationEventMulticaster
	{
		private final Integer moduleIndex;
		private final AcrossContextApplicationEventMulticaster contextApplicationEventMulticaster;

		ModuleApplicationEventMulticaster( BeanFactory beanFactory,
		                                   Integer moduleIndex,
		                                   AcrossContextApplicationEventMulticaster contextApplicationEventMulticaster ) {
			super( beanFactory );
			this.moduleIndex = moduleIndex;
			this.contextApplicationEventMulticaster = contextApplicationEventMulticaster;
		}

		@Override
		public void addApplicationListener( ApplicationListener<?> listener ) {
			if ( listener instanceof ApplicationListenerMethodAdapter ) {
				contextApplicationEventMulticaster.adjustMethodEventListener( (ApplicationListenerMethodAdapter) listener, moduleIndex );
			}

			super.addApplicationListener( listener );
		}

		@Override
		protected Collection<ApplicationListener<?>> getApplicationListeners( ApplicationEvent event, ResolvableType eventType ) {
			return super.getApplicationListeners( event, eventType );
		}

		@Override
		public void multicastEvent( ApplicationEvent event ) {
			contextApplicationEventMulticaster.multicastEvent( event );
		}

		/**
		 * Empty implementation of multi-casting a typed event. Events will be pushed up to the parent
		 * context where they will end up in the {@link AcrossContextApplicationEventMulticaster} that
		 * will call the module listeners in order. See {@link org.springframework.context.support.AbstractApplicationContext#publishEvent(Object, ResolvableType)}.
		 */
		@Override
		public void multicastEvent( ApplicationEvent event, ResolvableType eventType ) {
			// suppress typed multicast
		}
	}
}
