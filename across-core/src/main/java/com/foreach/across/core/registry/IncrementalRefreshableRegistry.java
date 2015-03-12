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

package com.foreach.across.core.registry;

import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import javax.annotation.PostConstruct;

/**
 * <p>Extends the {@link com.foreach.across.core.registry.RefreshableRegistry} by scanning for members
 * every time a module has bootstrapped.  Modules later in the bootstrapping order will immediately see
 * all members added by previous modules, whereas a regular RefreshableRegistry will only refresh its
 * members after the entire context has been bootstrapped.</p>
 */
@Refreshable
public class IncrementalRefreshableRegistry<T> extends RefreshableRegistry<T>
{
	@Autowired
	private AcrossEventPublisher eventBus;

	public IncrementalRefreshableRegistry( Class<T> memberType ) {
		super( memberType );
	}

	public IncrementalRefreshableRegistry( Class<T> type, boolean includeModuleInternals ) {
		super( type, includeModuleInternals );
	}

	public IncrementalRefreshableRegistry( ResolvableType resolvableType, boolean includeModuleInternals ) {
		super( resolvableType, includeModuleInternals );
	}

	@PostConstruct
	void hookupEventHandler() {
		eventBus.subscribe( this );
	}

	@Event
	void moduleBootstrapped( AcrossModuleBootstrappedEvent moduleBootstrapped ) {
		refresh();
	}
}
