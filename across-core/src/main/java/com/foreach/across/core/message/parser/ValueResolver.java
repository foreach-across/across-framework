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
package com.foreach.across.core.message.parser;

import com.foreach.across.core.message.ResolvableMessageFormatContext;
import lombok.Data;

/**
 * Interface for retrieving a value from the context.
 * Value is usually either an indexed or named argument, an expression
 * or another message lookup.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@FunctionalInterface
public interface ValueResolver<T>
{
	/**
	 * Retrieve the actual value during message formatting.
	 *
	 * @param context from which to retrieve the value
	 * @return value
	 */
	T resolveValue( ResolvableMessageFormatContext context );

	/**
	 * Resolver for a named argument.
	 */
	@Data
	class NamedArgument implements ValueResolver<Object>
	{
		private final String argumentName;

		@Override
		public Object resolveValue( ResolvableMessageFormatContext context ) {
			return context.getArgumentWithName( argumentName );
		}
	}

	static ValueResolver<Object> forNamedArgument( String argumentName ) {
		return new NamedArgument( argumentName );
	}

	/**
	 * Resolver for an indexed argument.
	 */
	@Data
	class IndexedArgument implements ValueResolver<Object>
	{
		private final int argumentIndex;

		@Override
		public Object resolveValue( ResolvableMessageFormatContext context ) {
			return context.getArgumentAt( argumentIndex );
		}
	}

	static ValueResolver<Object> forIndexedArgument( int argumentIndex ) {
		return new IndexedArgument( argumentIndex );
	}

	/**
	 * Resolver for an expression.
	 *
	 * @param expression to be executed
	 * @return resolver
	 */
	static ValueResolver<Object> forExpression( String expression ) {
		return context -> null;
	}

	/**
	 * Resolver for another message.
	 *
	 * @return resolver
	 */
	static ValueResolver<String> forResolvableMessage() {
		return context -> null;
	}
}
