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
package com.foreach.across.core.support;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents an attribute value in a {@link ReadableAttributes} hierarchy.
 * Provides information about the existence of an attribute as well as the ancestor
 * level at which the attribute was found.
 *
 * @author Arne Vandamme
 * @see AttributeOverridingSupport
 * @since 3.1.1
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class InheritedAttributeValue<T>
{
	@NonNull
	private final Optional<T> valueHolder;

	@Getter
	@NonNull
	private final String attributeName;

	@Getter
	private final int ancestorLevel;

	@SuppressWarnings("unchecked")
	public <U extends T> U getValue() {
		return (U) valueHolder.orElse( null );
	}

	public void ifPresent( Consumer<? super T> consumer ) {
		valueHolder.ifPresent( consumer );
	}

	public Optional<T> filter( Predicate<? super T> predicate ) {
		return valueHolder.filter( predicate );
	}

	public <U> Optional<U> map( Function<? super T, ? extends U> mapper ) {
		return valueHolder.map( mapper );
	}

	public <U> Optional<U> flatMap( Function<? super T, Optional<U>> mapper ) {
		return valueHolder.flatMap( mapper );
	}

	public T orElse( T other ) {
		return valueHolder.orElse( other );
	}

	public T orElseGet( Supplier<? extends T> other ) {
		return valueHolder.orElseGet( other );
	}

	public <X extends Throwable> T orElseThrow( Supplier<? extends X> exceptionSupplier ) throws X {
		return valueHolder.orElseThrow( exceptionSupplier );
	}

	public Optional<T> toOptional() {
		return valueHolder;
	}

	/**
	 * @return true if the attribute is a local attribute ({@link #getAncestorLevel()} returns {@code 0})
	 */
	public boolean isLocalAttribute() {
		return ancestorLevel == 0;
	}

	/**
	 * @return true if {@link #isLocalAttribute()} is {@code false}
	 */
	public boolean isInheritedAttribute() {
		return exists() && !isLocalAttribute();
	}

	/**
	 * Check if an attribute was registered. Note that an attribute can be registered with a {@code null}
	 * value in order to shadow a previously registered attribute. If so both {@link #isEmpty()} and {@code exists()}
	 * will return {@code true}.
	 *
	 * @return true if the attribute was registered
	 */
	public boolean exists() {
		return ancestorLevel >= 0;
	}

	/**
	 * @return true if a value is set for the attribute
	 */
	public boolean isEmpty() {
		return !valueHolder.isPresent();
	}
}
