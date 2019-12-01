/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.support;

import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Core component which handles the eager refresh of an {@link com.foreach.across.core.AcrossContext}.
 * These refresh calls will be handled before any other type of refresh happens.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class AcrossContextEagerRefreshHandler
{
	private final List<RefreshReference> referenceList = new ArrayList<>();

	public void addRefreshFunction( @NonNull Supplier function ) {
		addRefreshTarget( this, x -> function.get(), false );
	}

	public void addRefreshFunction( @NonNull Supplier function, boolean afterEachModule ) {
		addRefreshTarget( this, x -> function.get(), afterEachModule );
	}

	public <U> void addRefreshTarget( @NonNull U target, @NonNull Consumer<U> refreshFunction ) {
		referenceList.add( new RefreshReference( refreshFunction, new WeakReference<>( target ), false ) );
	}

	public <U> void addRefreshTarget( @NonNull U target, @NonNull Consumer<U> refreshFunction, boolean afterEachModule ) {
		referenceList.add( new RefreshReference( refreshFunction, new WeakReference<>( target ), afterEachModule ) );
	}

	@EventListener
	@SuppressWarnings("unchecked")
	public void refreshAfterModule( AcrossModuleBootstrappedEvent moduleBootstrappedEvent ) {
		referenceList.forEach( refreshReference -> {
			if ( refreshReference.afterEachModule && refreshReference.beanReference != null ) {
				Object target = refreshReference.beanReference.get();
				if ( target != null ) {
					refreshReference.refreshFunction.accept( target );
				}
			}
		} );
	}

	@SuppressWarnings("unchecked")
	public void refresh() {
		referenceList.forEach( refreshReference -> {
			if ( refreshReference.beanReference != null ) {
				Object target = refreshReference.beanReference.get();
				if ( target != null ) {
					refreshReference.refreshFunction.accept( target );
				}
			}
		} );
	}

	@RequiredArgsConstructor
	private static class RefreshReference
	{
		private final Consumer refreshFunction;
		private final WeakReference beanReference;
		private final boolean afterEachModule;
	}
}
