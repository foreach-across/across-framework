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
package com.foreach.across.core.message;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Builder
public class ResolvableMessageFormatContext
{
	@Getter
	private final Locale locale;
	private final Object[] arguments;
	private final String[] argumentNames;
	private final MessageResolver messageResolver;

	private ResolvableMessageFormatContext( @NonNull Locale locale,
	                                        @NonNull Object[] arguments,
	                                        @NonNull String[] argumentNames,
	                                        MessageResolver messageResolver ) {
		this.locale = locale;
		this.arguments = arguments;
		this.argumentNames = argumentNames;
		this.messageResolver = messageResolver;

		Assert.isTrue( arguments.length == argumentNames.length, "Number of arguments and argument names must be the same" );
	}

	public Object getArgumentAt( int index ) {
		return ( index >= 0 && index < arguments.length ) ? arguments[index] : null;
	}

	public Object getArgumentWithName( String argumentName ) {
		for ( int i = 0; i < argumentNames.length; i++ ) {
			if ( argumentNames[i] != null && argumentNames[i].equals( argumentName ) ) {
				return arguments[i];
			}
		}
		return null;
	}

	public Object createFormatter() {
		return null;
	}

	public Object resolveExpression() {
		return null;
	}

	/**
	 * Resolve a separate message using the same locale.
	 * Will always return the default value if there is no {@link MessageResolver} set on the context.
	 *
	 * @param resolvableMessage message to resolve
	 * @return formatted message
	 */
	public String resolveMessage( ResolvableMessage resolvableMessage, boolean inheritParameters ) {
		if ( inheritParameters ) {
			for ( int i = 0; i < arguments.length; i++ ) {
				resolvableMessage.withParameter( argumentNames[i], arguments[i] );
			}
		}
		return messageResolver != null ? messageResolver.resolveMessage( resolvableMessage, locale ) : resolvableMessage.getDefaultValue();

	}

	@SuppressWarnings("unused")
	public static class ResolvableMessageFormatContextBuilder
	{
		private Object[] arguments = new Object[0];
		private String[] argumentNames = new String[0];
	}
}
