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
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a {@link ViewElement} in its most simple form.  In a web context this is almost certainly
 * a HTML node or collection thereof.
 *
 * @see MutableViewElement
 */
public interface ViewElement
{
	/**
	 * A ViewElement can have an internal name that identifies it within a
	 * {@link com.foreach.across.modules.web.ui.elements.ContainerViewElement}.  A name is optional but when given,
	 * is preferably unique within its container as most operations work on the first element with a specific name.
	 *
	 * @return Internal name of this element, can be null.
	 * @see com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils
	 */
	String getName();

	/**
	 * @return Type id of this view element.
	 */
	String getElementType();

	/**
	 * @return Custom template to use when rendering this view element.
	 */
	String getCustomTemplate();

	/**
	 * Execute one or more setter functions.
	 *
	 * @param setters to execute
	 * @return self
	 */
	default ViewElement set( WitherSetter... setters ) {
		with( this ).set( setters );
		return this;
	}

	/**
	 * Execute one or more remover functions. A remover is defined as a separate interface
	 * so implementations can both implement setter and remover at the same time.
	 *
	 * @param functions to execute
	 * @return self
	 */
	default ViewElement remove( WitherRemover... functions ) {
		with( this ).remove( functions );
		return this;
	}

	/**
	 * Get a value from the element. Use {@link WitherGetter#as(Class)} for compile time typing.
	 * Note that runtime type casting execptions will occur when using the wrong type.
	 *
	 * @param function that returns the value
	 * @param <U>      type of the expected return value
	 * @return value
	 */
	default <U> U get( WitherGetter<?, U> function ) {
		return with( this ).get( function );
	}

	/**
	 * Create a {@link Wither} wrapper for an element, which allows for a more
	 * descriptive, fluent configuration approach by using pre-defined lambdas.
	 *
	 * @param element to create a wrapper for
	 * @param <U>     type of the element
	 * @return wither
	 */
	static <U extends ViewElement> Wither<U> with( @NonNull U element ) {
		return new Wither<>( element );
	}

	/**
	 * A wrapper for any type of {@link ViewElement} which allows the use of separate
	 * functions to perform the actions. Uses 3 separate functional interfaces to perform
	 * typical actions and convey meaning (get, set, remove).
	 * <p/>
	 * The main purpose of the wither approach is to allow a library of functions to be
	 * defined that allow a more descriptive and fluent approach of configuration elements.
	 * Using separate functions increases testability and removes the need for a complex class hierarchy.
	 * <p/>
	 * Default functions are available for typical html node actions.
	 * <p/>
	 * Because of limitations with generics, the wither does not define a type on its parameter interfaces.
	 * This means that the function implementations should do type checking at runtime where appropriate,
	 * or runtime class cast exceptions will be thrown.
	 *
	 * @param <T> type of the view element
	 * @see WitherRemover function that is expected to remove one or more settings from the element (eg. attributes)
	 * @see WitherGetter function to fetch a value, allows for typing the result
	 * @see WitherSetter function that is expected to set one or more values on the element
	 * @see MutableViewElement.Functions
	 */
	@RequiredArgsConstructor
	class Wither<T extends ViewElement>
	{
		private final T element;

		/**
		 * Execute one or more setter functions.
		 *
		 * @param setters to execute
		 * @return self
		 */
		@SuppressWarnings("unchecked")
		public Wither<T> set( WitherSetter... setters ) {
			Stream.of( setters ).forEach( c -> c.applyTo( element ) );
			return this;
		}

		/**
		 * Allows for a specifically typed generic consumer to be passed or defined.
		 *
		 * @param consumer to execute
		 * @return self
		 */
		public Wither<T> apply( Consumer<? super T> consumer ) {
			consumer.accept( element );
			return this;
		}

		/**
		 * Execute one or more remover functions. A remover is defined as a separate interface
		 * so implementations can both implement setter and remover at the same time.
		 *
		 * @param functions to execute
		 * @return self
		 */
		@SuppressWarnings("unchecked")
		public Wither<T> remove( WitherRemover... functions ) {
			Stream.of( functions ).forEach( r -> r.removeFrom( element ) );
			return this;
		}

		/**
		 * Get a value from the element. Use {@link WitherGetter#as(Class)} for compile time typing.
		 * Note that runtime type casting execptions will occur when using the wrong type.
		 *
		 * @param function that returns the value
		 * @param <U>      type of the expected return value
		 * @return value
		 */
		@SuppressWarnings("unchecked")
		public <U> U get( WitherGetter<?, U> function ) {
			return (U) ( (WitherGetter) function ).getValueFrom( element );
		}

		public T get() {
			return element;
		}
	}

	/**
	 * Function that modifies a view element by setting or adding values.
	 * Typical example: setting an attribute value.
	 *
	 * @param <T> type of the view element
	 */
	@FunctionalInterface
	interface WitherSetter<T extends ViewElement>
	{
		void applyTo( T target );
	}

	/**
	 * Function that modifies a view element by removing values.
	 * Typical example: removing an attribute.
	 * See also {@link MutableViewElement.Functions#remove(WitherRemover[])} to
	 * convert a remover functions into {@link WitherSetter}.
	 *
	 * @param <T> type of the view element
	 */
	@FunctionalInterface
	interface WitherRemover<T extends ViewElement>
	{
		void removeFrom( T target );
	}

	/**
	 * Function that fetches a value from a view element and optionally returns
	 * it as a specific type for compile time checking.
	 * Typical example: getting an attribute value.
	 *
	 * @param <T> type of the view element
	 */
	@FunctionalInterface
	interface WitherGetter<T extends ViewElement, U>
	{
		U getValueFrom( T target );

		/**
		 * Cast the return value as a specific type, to avoid explicit casting in consumer code.
		 */
		@SuppressWarnings({ "unchecked", "unused" })
		default <V extends U, W extends V> WitherGetter<T, W> as( Class<V> clazz ) {
			return (WitherGetter<T, W>) this;
		}
	}
}
