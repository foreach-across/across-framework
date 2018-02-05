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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@EqualsAndHashCode(exclude = "tokens")
public class MessageTokenCollector
{
	public static final char BEGIN_TERM = '{';
	public static final char END_TERM = '}';
	public static final char START_MESSAGE_LOOKUP = '#';
	public static final char START_EXPRESSION = '$';
	public static final char ESCAPE_CHARACTER = '\\';

	@Getter
	private final List<MessageToken> tokens = new ArrayList<>();

	@Getter
	private final String messageDescriptor;

	private int currentPosition;
	private char previous;

	private Deque<ParserContext> stack;
	private ParserContext currentContext;

	public MessageTokenCollector( String messageDescriptor ) {
		this.messageDescriptor = messageDescriptor;

		parse();
	}

	private void parse() throws MessageDescriptorFormatException {
		stack = new ArrayDeque<>();

		currentContext = new LiteralParserContext();

		while ( currentPosition <= messageDescriptor.length() ) {
			next();
		}

		stack = null;
	}

	private void next() throws MessageDescriptorFormatException {
		if ( currentPosition == messageDescriptor.length() ) {
			if ( !currentContext.isEmpty() ) {
				if ( currentContext.isTerm() ) {
					throw new IllegalArgumentException( "Unfinished message token: " + currentContext.tokenData.toString() );
				}
				currentContext.addToken();
			}
			currentContext = null;
			currentPosition++;
			return;
		}

		char currentCharacter = messageDescriptor.charAt( currentPosition );
		currentPosition++;

		switch ( currentCharacter ) {
			case BEGIN_TERM:
			case END_TERM:
			case START_EXPRESSION:
			case START_MESSAGE_LOOKUP:
				if ( previous == ESCAPE_CHARACTER ) {
					currentContext.append( currentCharacter );
				}
				else {
					currentContext.handle( currentCharacter );
				}
				break;
			case ESCAPE_CHARACTER:
				if ( previous == ESCAPE_CHARACTER ) {
					currentContext.append( currentCharacter );
				}
				else {
					currentContext.flushPrevious();
					previous = currentCharacter;
				}
				break;
			default:
				currentContext.handle( currentCharacter );
		}
	}

	@RequiredArgsConstructor
	private class ParserContext
	{
		protected final StringBuilder tokenData = new StringBuilder();
		protected final Function<ParserContext, ? extends MessageToken> tokenFactory;

		boolean isEmpty() {
			return tokenData.length() == 0;
		}

		void addToken() {
			flushPrevious();
			if ( !isEmpty() ) {
				tokens.add( tokenFactory.apply( this ) );
			}
		}

		void append( char ch ) {
			tokenData.append( ch );
			previous = 0;
		}

		void handle( char ch ) {
			append( ch );
		}

		boolean isTerm() {
			return true;
		}

		protected void flushPrevious() {
			if ( previous != 0 ) {
				tokenData.append( previous );
				previous = 0;
			}
		}
	}

	private class LiteralParserContext extends ParserContext
	{
		LiteralParserContext() {
			super( context -> new Literal( context.tokenData.toString() ) );
		}

		@Override
		void handle( char ch ) {
			switch ( ch ) {
				case BEGIN_TERM:
					if ( START_MESSAGE_LOOKUP == previous ) {
						previous = 0;
						currentContext = new MessageLookupParserContext();
					}
					else if ( START_EXPRESSION == previous ) {
						previous = 0;
						currentContext = new ExpressionParserContext();
					}
					else {
						flushPrevious();
						currentContext = new ArgumentParserContext();
					}
					addToken();
					break;
				case START_EXPRESSION:
				case START_MESSAGE_LOOKUP:
					flushPrevious();
					previous = ch;
					break;
				default:
					flushPrevious();
					super.append( ch );
			}
		}

		@Override
		boolean isTerm() {
			return false;
		}
	}

	private class ArgumentParserContext extends ParserContext
	{
		ArgumentParserContext() {
			super( context -> {
				String arg = context.tokenData.toString();
				if ( StringUtils.isNumeric( arg ) ) {
					return new IndexedArgument( Integer.parseInt( arg ) );
				}
				return new NamedArgument( arg );
			} );
		}

		@Override
		void handle( char ch ) {
			switch ( ch ) {
				case END_TERM:
					addToken();
					currentContext = new LiteralParserContext();
					break;
				default:
					flushPrevious();
					super.append( ch );
			}
		}
	}

	private class MessageLookupParserContext extends ParserContext
	{
		MessageLookupParserContext() {
			super( context -> new MessageLookup( StringUtils.split( context.tokenData.toString(), "," ) ) );
		}

		@Override
		void handle( char ch ) {
			switch ( ch ) {
				case END_TERM:
					addToken();
					currentContext = new LiteralParserContext();
					break;
				default:
					flushPrevious();
					super.append( ch );
			}
		}
	}

	private class ExpressionParserContext extends ParserContext
	{
		ExpressionParserContext() {
			super( context -> new Expression( context.tokenData.toString() ) );
		}

		@Override
		void handle( char ch ) {
			switch ( ch ) {
				case END_TERM:
					addToken();
					currentContext = new LiteralParserContext();
					break;
				default:
					flushPrevious();
					super.append( ch );
			}
		}
	}
}
