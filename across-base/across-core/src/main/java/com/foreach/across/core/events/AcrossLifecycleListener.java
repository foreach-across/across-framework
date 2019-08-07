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

import org.springframework.context.ApplicationListener;

/**
 * Dispatching interface for intercepting {@link AcrossLifecycleEvent} in situations where
 * {@link org.springframework.context.event.EventListener} can not be used (eg. in the parent
 * {@link org.springframework.context.ApplicationContext} of an Across context).
 * <p/>
 * This interface has a default implementation of {@link #onApplicationEvent(AcrossLifecycleEvent)}
 * that dispatches to corresponding event-specific methods.
 * <p/>
 * NOTE: only known {@link AcrossLifecycleEvent} implementations will be handled, all others will be ignored
 * by the default {@link #onApplicationEvent(AcrossLifecycleEvent)} implementation.
 *
 * @author Arne Vandamme
 * @see AcrossLifecycleEvent
 * @since 3.0.0
 */
public interface AcrossLifecycleListener extends ApplicationListener<AcrossLifecycleEvent>
{
	@Override
	default void onApplicationEvent( AcrossLifecycleEvent event ) {
		if ( event instanceof AcrossModuleBeforeBootstrapEvent ) {
			onAcrossModuleBeforeBootstrapEvent( (AcrossModuleBeforeBootstrapEvent) event );
		}
		else if ( event instanceof AcrossModuleBootstrappedEvent ) {
			onAcrossModuleBootstrappedEvent( (AcrossModuleBootstrappedEvent) event );
		}
		else if ( event instanceof AcrossContextBootstrappedEvent ) {
			onAcrossContextBootstrappedEvent( (AcrossContextBootstrappedEvent) event );
		}
	}

	/**
	 * Executed when a lifecycle event of type {@link AcrossModuleBeforeBootstrapEvent} is received.
	 *
	 * @param event published
	 */
	default void onAcrossModuleBeforeBootstrapEvent( AcrossModuleBeforeBootstrapEvent event ) {
	}

	/**
	 * Executed when a lifecycle event of type {@link AcrossModuleBootstrappedEvent} is received.
	 *
	 * @param event published
	 */
	default void onAcrossModuleBootstrappedEvent( AcrossModuleBootstrappedEvent event ) {
	}

	/**
	 * Executed when a lifecycle event of type {@link AcrossContextBootstrappedEvent} is received.
	 *
	 * @param event published
	 */
	default void onAcrossContextBootstrappedEvent( AcrossContextBootstrappedEvent event ) {
	}
}
