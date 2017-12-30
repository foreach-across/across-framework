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

import org.springframework.context.ApplicationEvent;

/**
 * Base class for events published by Across core during the bootstrap (and shutdown) phase of an Across context.
 * Extends {@link ApplicationEvent} directly so {@link org.springframework.context.ApplicationListener} could be used for these events.
 * This is necessary because often the Across context initializes as a singleton in the parent {@link org.springframework.context.ApplicationContext}
 * while the {@link org.springframework.context.event.EventListener} methods are only registered after all singletons have been created.
 * <p/>
 * {@link org.springframework.context.ApplicationListener} beans are registered early on, so this allows a parent context to receive
 * events published during Across context bootstrap.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public abstract class AcrossLifecycleEvent extends ApplicationEvent implements AcrossEvent
{
	AcrossLifecycleEvent( Object source ) {
		super( source );
	}
}
