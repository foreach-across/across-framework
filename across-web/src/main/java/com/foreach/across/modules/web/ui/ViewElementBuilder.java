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
package com.foreach.across.modules.web.ui;

import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base interface to create a single {@link ViewElement} instance.
 * Usually used to build an entire hierarchy of elements by calling {@link #build(ViewElementBuilderContext)} on the
 * top-most element. Building a {@link ViewElement} requires a {@link ViewElementBuilderContext} and often a
 * single builder context is used to build many elements.
 * <p/>
 * If you call {@link #build()} without manually specifying a builder context, a global context will be retrieved
 * using {@link ViewElementBuilderContext#retrieveGlobalBuilderContext()} and if none is available, a new
 * {@link DefaultViewElementBuilderContext} will be used instead.
 * <p/>
 * For performance it is often best to manage the lifecycle of a {@link ViewElementBuilderContext} yourself,
 * so you don't have unnecessary creation and can optimize contextual data sharing.
 *
 * @author Arne Vandamme
 */
@FunctionalInterface
public interface ViewElementBuilder<T extends ViewElement>
{
	/**
	 * Build the {@link ViewElement} using the globally available {@link ViewElementBuilderContext},
	 * this will use {@link ViewElementBuilderContext#retrieveGlobalBuilderContext()} to get the global context.
	 * <p/>
	 * If none is returned, a new {@link DefaultViewElementBuilderContext} will be used instead.
	 * <p/>
	 * Use this method sparingly, usually only for the single top-level build of a {@link ViewElement}.
	 * The creation of a {@link ViewElementBuilderContext} can be relatively costly, performance-wise it is usually
	 * better if you call {@link #build(ViewElementBuilderContext)} with a predefined or {@link ViewElementBuilderContext},
	 * or at least ensure you have a global context available.
	 *
	 * @return view element
	 * @see ViewElementBuilderContext#retrieveGlobalBuilderContext()
	 */
	default T build() {
		ViewElementBuilderContext globalBuilderContext = ViewElementBuilderContext
				.retrieveGlobalBuilderContext()
				.orElseGet( DefaultViewElementBuilderContext::new );

		return build( globalBuilderContext );
	}

	/**
	 * Builds the actual element.
	 *
	 * @param builderContext provides the context for this build event
	 * @return instance to render the element.
	 */
	T build( ViewElementBuilderContext builderContext );

	/**
	 * Chain a {@link ViewElementPostProcessor} to the result of this builder.
	 * This will return a new builder instance that applies the post processor to the element generated.
	 * <p>
	 * Explicitly supports {@code null} argument values, which mean <em>do nothing</em> and will actually
	 * return the same builder.
	 *
	 * @param postProcessor to apply on the builder result
	 * @return new builder with the post processor chained to it
	 */
	default ViewElementBuilder<T> andThen( ViewElementPostProcessor<T> postProcessor ) {
		if ( postProcessor != null ) {
			ViewElementBuilder<T> self = this;
			return builderContext -> {
				T element = self.build( builderContext );
				postProcessor.postProcess( builderContext, element );
				return element;
			};
		}
		return this;
	}

	/**
	 * Map the {@link ViewElement} that this builder returns to another type.
	 * Creates a new builder returning the resulting element.
	 * <p/>
	 * If you need access to the {@link ViewElementBuilderContext} use {@link #map(BiFunction)} instead.
	 *
	 * @param mappingFunction to apply to the generated element
	 * @param <U>             type of the new element returned
	 * @return new builder instance
	 * @see #map(BiFunction)
	 */
	default <U extends ViewElement> ViewElementBuilder<U> map( @NonNull Function<T, U> mappingFunction ) {
		return builderContext -> {
			T element = this.build( builderContext );
			return mappingFunction.apply( element );
		};
	}

	/**
	 * Map the {@link ViewElement} that this builder returns to another type.
	 * Creates a new builder returning the resulting element.
	 *
	 * @param mappingFunction to apply to the generated element
	 * @param <U>             type of the new element returned
	 * @return new builder instance
	 * @see #map(Function)
	 */
	default <U extends ViewElement> ViewElementBuilder<U> map( @NonNull BiFunction<ViewElementBuilderContext, T, U> mappingFunction ) {
		return builderContext -> {
			T element = this.build( builderContext );
			return mappingFunction.apply( builderContext, element );
		};
	}

	default ViewElementBuilder<T> doWith( Wither... operations ) {
		return null;
	}

	default ViewElementBuilder<T> postProcess( ViewElementPostProcessor<T> postProcessors ) {
		return postProcess( Collections.singleton( postProcessors ) );
	}

	default ViewElementBuilder<T> postProcess( Collection<ViewElementPostProcessor<T>> postProcessors ) {
		return builderContext -> {
			T element = build( builderContext );
			postProcessors.forEach( processor -> processor.postProcess( builderContext, element ) );
			return element;
		};
	}

	static <U extends ViewElement> ViewElementBuilder<U> of( Supplier<U> supplier ) {
		return ( builderContext ) -> supplier.get();
	}

	static <U extends ViewElement> ViewElementBuilder<U> of( Function<ViewElementBuilderContext, U> supplier ) {
		return supplier::apply;
	}

	@FunctionalInterface
	interface Wither<T extends ViewElementBuilder>
	{
		void applyTo( T builder );
		// div( children(), postProcess() )
		// ViewElementBuilder.of( () -> new NodeViewElement("div" ).with( text( "hello" ), attribute( "hm" ) )
	}
}
