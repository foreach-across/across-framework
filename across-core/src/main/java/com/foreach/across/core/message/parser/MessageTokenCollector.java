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
	public static final char SEPARATOR = ',';

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
	private abstract class ParserContext
	{
		final StringBuilder tokenData = new StringBuilder();

		boolean isEmpty() {
			return tokenData.length() == 0;
		}

		void addToken() {
			flushPrevious();
			if ( !isEmpty() ) {
				tokens.add( createToken() );
			}
		}

		abstract MessageToken createToken();

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

		void flushPrevious() {
			if ( previous != 0 ) {
				tokenData.append( previous );
				previous = 0;
			}
		}
	}

	private class LiteralParserContext extends ParserContext
	{
		@Override
		MessageToken createToken() {
			return new Literal( tokenData.toString() );
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
		private int group = 0;
		private String[] parts = new String[3];

		private int nestedTerm = 0;

		@Override
		MessageToken createToken() {
			parts[group] = tokenData.toString().trim();
			if ( StringUtils.isNumeric( parts[0] ) ) {
				return new FormattedArgument( ValueResolver.forIndexedArgument( Integer.parseInt( parts[0] ) ), parts[1], parts[2] );
			}
			return new FormattedArgument( ValueResolver.forNamedArgument( parts[0] ), parts[1], parts[2] );
		}

		@Override
		void handle( char ch ) {
			switch ( ch ) {
				case BEGIN_TERM:
					if ( group == 2 ) {
						nestedTerm++;
						super.append( ch );
						break;
					}
				case SEPARATOR:
					parts[group] = tokenData.toString().trim();
					tokenData.setLength( 0 );
					group++;
					break;
				case END_TERM:
					if ( nestedTerm > 0 ) {
						nestedTerm--;
						super.append( ch );
					}
					else {
						addToken();
						currentContext = new LiteralParserContext();
					}
					break;
				default:
					flushPrevious();
					super.append( ch );
			}
		}
	}

	private class MessageLookupParserContext extends ParserContext
	{
		@Override
		MessageToken createToken() {
			return new MessageLookup( StringUtils.split( tokenData.toString(), "," ) );
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
		@Override
		MessageToken createToken() {
			return new Expression( tokenData.toString() );
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
