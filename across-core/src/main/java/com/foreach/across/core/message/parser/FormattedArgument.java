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
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;

/**
 * Represents an argument with an optional formatting applies.
 * The current implementation only creates {@link MessageFormat} instances for formatting.
 * todo: use the context to create a formatter
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Data
class FormattedArgument implements MessageToken
{
	private final ValueResolver<?> valueResolver;
	private final String formatType;
	private final String formatStyle;

	@Override
	public MessageTokenOutput createFormat( ResolvableMessageFormatContext context ) {
		return new Output( valueResolver, new MessageFormat( createPattern(), context.getLocale() ) );
	}

	private String createPattern() {
		if ( formatStyle != null ) {
			return "{0," + formatType + "," + formatStyle + "}";
		}
		if ( formatType != null ) {
			return "{0," + formatType + "}";
		}
		return "{0}";
	}

	@RequiredArgsConstructor
	static class Output implements MessageTokenOutput
	{
		private final ValueResolver<?> valueResolver;
		private final MessageFormat messageFormat;

		@Override
		public boolean isLocalized() {
			return true;
		}

		@Override
		public boolean requiresSynchronization() {
			return true;
		}

		@Override
		public void write( StringBuilder output, ResolvableMessageFormatContext context ) {
			output.append( messageFormat.format( new Object[] { valueResolver.resolveValue( context ) } ) );
		}
	}
}
