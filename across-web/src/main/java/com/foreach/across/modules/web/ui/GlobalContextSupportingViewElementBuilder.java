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
package com.foreach.across.modules.web.ui;

/**
 * Base implementation of {@link ViewElementBuilder} that supports a globally available {@link ViewElementBuilderContext}.
 * A global {@link ViewElementBuilderContext} should still be created manually, and either be bound to the local
 * thread or to the request.
 *
 * @author Arne Vandamme
 * @see ViewElementBuilderContextInterceptor
 * @since 2.0.0
 */
public abstract class GlobalContextSupportingViewElementBuilder<T extends ViewElement> implements ViewElementBuilder<T>
{
	/**
	 * Build the {@link ViewElement} using the globally available {@link ViewElementBuilderContext}.
	 * If a thread-local {@link ViewElementBuilderContext} is found, that one will be use.  If not, a
	 * {@link ViewElementBuilderContext} will be looked for on the available web request attributes.
	 * An exception will be thrown if no global {@link ViewElementBuilderContext} is available.
	 *
	 * @return view element
	 */
	public final T build() {
		ViewElementBuilderContext globalBuilderContext = ViewElementBuilderContext
				.retrieveGlobalBuilderContext()
				.<IllegalStateException>orElseThrow( () -> {
					throw new IllegalStateException(
							"No global ViewElementBuilderContext available: neither as thread-local nor as request attribute" );
				} );

		return build( globalBuilderContext );
	}
}
