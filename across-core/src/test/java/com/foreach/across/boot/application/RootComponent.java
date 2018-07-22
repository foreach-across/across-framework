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
package com.foreach.across.boot.application;

import lombok.Getter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Component in the root of the package.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Component
public class RootComponent
{
	public static final AtomicInteger EVENT_COUNTER = new AtomicInteger( 0 );

	@Getter
	private Map<String, Integer> eventsReceived = new LinkedHashMap<>();

	@EventListener
	public void handle( ApplicationReadyEvent applicationReadyEvent ) {
		eventsReceived.put( applicationReadyEvent.getClass().getName(), EVENT_COUNTER.getAndIncrement() );
	}
}
