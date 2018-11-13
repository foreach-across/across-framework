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

import org.springframework.core.NamedInheritableThreadLocal;

import java.util.Optional;

/**
 * Holder class that associates a {@link ViewElementBuilderContext} with the current thread.
 * Used by block tag processors where nested blocks would build on top of the parent context.
 * This is different than the request scoped bean: the thread local render context is the one
 * actually being used by the current thread for the rendering, whereas the request scoped bean
 * represents the global context for the request (the root of the render context hierarchy).
 * <p/>
 * The attached context is not inherited by child threads.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.config.GlobalViewElementBuilderContextConfiguration
 * @since 2.0.0
 */
public abstract class ViewElementBuilderContextHolder
{
	private static final ThreadLocal<ViewElementBuilderContext> ViewElementBuilderContextHolder =
			new NamedInheritableThreadLocal<>( "ViewElementBuilderContext" );

	/**
	 * Associate the given context with the current thread.  Will replace any previously configured context.
	 * If the context parameter is {@code null}, the attached context will be removed.
	 *
	 * @param ViewElementBuilderContext instance
	 * @return the context that has been removed
	 */
	public static Optional<ViewElementBuilderContext> setViewElementBuilderContext( ViewElementBuilderContext ViewElementBuilderContext ) {
		return setViewElementBuilderContext( Optional.ofNullable( ViewElementBuilderContext ) );
	}

	/**
	 * Associate the given context with the current thread.  Will replace any previously configured context.
	 * If the context parameter is {@code empty}, the attached context will be removed.
	 * <p/>
	 * Convenience method for easy resetting of previously retrieved context.
	 *
	 * @param ViewElementBuilderContext instance
	 * @return the context that has been removed
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static Optional<ViewElementBuilderContext> setViewElementBuilderContext( Optional<ViewElementBuilderContext> ViewElementBuilderContext ) {
		Optional<ViewElementBuilderContext> current = getViewElementBuilderContext();
		if ( ViewElementBuilderContext.isPresent() ) {
			ViewElementBuilderContextHolder.set( ViewElementBuilderContext.get() );
		}
		else {
			clearViewElementBuilderContext();
		}

		return current;
	}

	/**
	 * @return the context attached to the current thread
	 */
	public static Optional<ViewElementBuilderContext> getViewElementBuilderContext() {
		return Optional.ofNullable( ViewElementBuilderContextHolder.get() );
	}

	/**
	 * Removes the (optional) context attached to the current thread.
	 *
	 * @return the context that has been removed
	 */
	public static Optional<ViewElementBuilderContext> clearViewElementBuilderContext() {
		Optional<ViewElementBuilderContext> current = getViewElementBuilderContext();
		ViewElementBuilderContextHolder.remove();
		return current;
	}
}

