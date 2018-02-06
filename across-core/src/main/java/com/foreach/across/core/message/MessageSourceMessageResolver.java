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

import com.foreach.across.core.message.parser.MessageTokenCollector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor
public class MessageSourceMessageResolver implements MessageResolver
{
	private final static String INVALID = Character.toString( (char) 0 );

	@NonNull
	private final MessageSource messageSource;

	public String resolveMessage( ResolvableMessage resolvable ) {
		return resolveMessage( resolvable, null );
	}

	@Override
	public String resolveMessage( ResolvableMessage resolvable, Locale locale ) {
		return resolveMessage( resolvable, locale, null );
	}

	public String resolveMessage( ResolvableMessage resolvableMessage, Locale locale, Consumer<String> consumer ) {
		String message = messageSource.getMessage( new DefaultMessageSourceResolvable( resolvableMessage.getCodes(), INVALID ), locale );

		if ( INVALID.equals( message ) ) {
			message = resolvableMessage.getDefaultValue();
		}

		if ( message != null ) {
			ResolvableMessageFormat formatter = new ResolvableMessageFormat( new MessageTokenCollector( message ).getTokens() );
			List<String> parameterNames = resolvableMessage.getParameterNames();
			ResolvableMessageFormatContext context = ResolvableMessageFormatContext
					.builder()
					.locale( Locale.getDefault() )
					.messageResolver( this )
					.arguments( resolvableMessage.getParameters().toArray() )
					.argumentNames( parameterNames.toArray( new String[parameterNames.size()] ) )
					.build();

			return formatter.format( context );
		}

		return null;
	}
}
