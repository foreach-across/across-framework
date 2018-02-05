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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.text.MessageFormat;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor
public class MessageSourceMessageResolver implements MessageResolver
{
	@NonNull
	private final MessageSource messageSource;

	public String resolveMessage( ResolvableMessage resolvable ) {
		return resolveMessage( resolvable, null );
	}

	public String resolveMessage( ResolvableMessage resolvableMessage, Consumer<String> consumer ) {

		// build message source resolvable
		String message = messageSource.getMessage( new DefaultMessageSourceResolvable( resolvableMessage.getCodes() ), null );

		Map<String, Object> parameters = resolvableMessage.getParameters();

		if ( parameters != null ) {
			int index = 0;
			for ( val paramName : parameters.keySet() ) {
				message = message.replaceAll( "\\{" + paramName, "\\{" + index );
				index++;
			}

			MessageFormat messageFormat = new MessageFormat( message );

			return messageFormat.format( parameters.values().toArray() );
		}
		return message;
	}
}
