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

/**
 * Event publisher interface for sending {@link AcrossEvent} implementations.
 * As of 3.0.0 using the custom Across event publisher is no longer required and discouraged,
 * use the {@link org.springframework.context.ApplicationEventPublisher} from Spring instead.
 * <p/>
 * Any event sent from within an Across this will be routed through the Across event pipeline
 * and sent to the listeners registered by all modules according to their module order.
 *
 * @see org.springframework.context.ApplicationEventPublisher
 * @deprecated use the Spring {@link org.springframework.context.ApplicationEventPublisher} instead
 */
@Deprecated
public interface AcrossEventPublisher
{
	/**
	 * Publish an event.
	 *
	 * @param event to publish
	 */
	void publish( AcrossEvent event );

	/**
	 * Publish an event asynchronously.
	 *
	 * @param event to publish
	 * @deprecated users should provide their own async calling methods
	 */
	@Deprecated
	void publishAsync( AcrossEvent event );
}
