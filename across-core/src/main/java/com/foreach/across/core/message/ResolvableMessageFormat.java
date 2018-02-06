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

import com.foreach.across.core.message.parser.MessageToken;
import com.foreach.across.core.message.parser.MessageTokenOutput;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor
public class ResolvableMessageFormat
{
	private final List<MessageToken> tokens;
	private final Map<Locale, Executable> formatsByLocale = new ConcurrentHashMap<>();

	private Executable nonLocalizedExecutable;

	public String format( ResolvableMessageFormatContext context ) {

		if ( nonLocalizedExecutable != null ) {
			return format( nonLocalizedExecutable, context );
		}

		Executable executable = formatsByLocale.get( context.getLocale() );

		if ( executable == null ) {
			executable = buildAndRegisterExecutable( context );
		}

		return format( executable, context );
	}

	private Executable buildAndRegisterExecutable( ResolvableMessageFormatContext context ) {
		List<MessageTokenOutput> outputTokens = new ArrayList<>( tokens.size() );

		boolean localized = false;
		boolean requiresSynchronization = false;
		for ( MessageToken token : tokens ) {
			MessageTokenOutput outputToken = token.createFormat( context );
			requiresSynchronization |= outputToken.requiresSynchronization();
			localized |= outputToken.isLocalized();
			outputTokens.add( outputToken );
		}

		Executable executable = new Executable( outputTokens, requiresSynchronization );

		if ( localized ) {
			formatsByLocale.put( context.getLocale(), executable );
		}
		else {
			nonLocalizedExecutable = executable;
		}

		return executable;
	}

	@SuppressWarnings("all")
	private String format( Executable executable, ResolvableMessageFormatContext context ) {
		if ( executable.requiresSynchronization ) {
			synchronized ( executable ) {
				return executable.format( context );
			}
		}

		return executable.format( context );
	}

	@RequiredArgsConstructor
	private static class Executable
	{
		private final List<MessageTokenOutput> segments;
		private final boolean requiresSynchronization;

		String format( ResolvableMessageFormatContext context ) {
			StringBuilder output = new StringBuilder();
			for ( MessageTokenOutput format : segments ) {
				format.write( output, context );
			}
			return output.toString();
		}
	}
}
