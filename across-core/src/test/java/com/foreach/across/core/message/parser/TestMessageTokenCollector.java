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

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestMessageTokenCollector
{
	@Test
	public void singleLiteralIfNoTokens() {
		assertThat( parse( "my message" ) )
				.containsExactly( new Literal( "my message" ) );
	}

	@Test
	public void singleSimpleIndexedArgument() {
		assertThat( parse( "{0}" ) )
				.containsExactly( new IndexedArgument( 0 ) );
	}

	@Test
	public void singleSimpleNamedArgument() {
		assertThat( parse( "{title}" ) )
				.containsExactly( new NamedArgument( "title" ) );
	}

	@Test
	public void singleSimpleMessageCode() {
		assertThat( parse( "#{title}" ) )
				.containsExactly( new MessageLookup( new String[] { "title" } ) );
	}

	@Test
	public void singleExpression() {
		assertThat( parse( "${application.title}" ) )
				.containsExactly( new Expression( "application.title" ) );
	}

	@Test
	public void nonValidTerms() {
		assertThat( parse( "$ \\ }#" ) )
				.containsExactly( new Literal( "$ \\ }#" ) );
	}

	@Test
	public void escapedTokens() {
		assertThat( parse( "\\{0}" ) ).containsExactly( new Literal( "{0}" ) );
		assertThat( parse( "\\{title}" ) ).containsExactly( new Literal( "{title}" ) );
		assertThat( parse( "#\\{title}" ) ).containsExactly( new Literal( "#{title}" ) );
		assertThat( parse( "$\\{title}" ) ).containsExactly( new Literal( "${title}" ) );
		assertThat( parse( "\\#{title}" ) ).containsExactly( new Literal( "#" ), new NamedArgument( "title" ) );
		assertThat( parse( "\\${title}" ) ).containsExactly( new Literal( "$" ), new NamedArgument( "title" ) );
	}

	@Test
	public void multipleSimpleTokens() {
		assertThat( parse( "my message {0} \\${title} \\##{superTitle} \\{ ${expression}" ) )
				.containsExactly(
						new Literal( "my message " ),
						new IndexedArgument( 0 ),
						new Literal( " $" ),
						new NamedArgument( "title" ),
						new Literal( " #" ),
						new MessageLookup( new String[] { "superTitle" } ),
						new Literal( " { " ),
						new Expression( "expression" )
				);
	}

	@Test
	public void allNonEndingTermCharactersInsideExpressionAreIgnored() {
		assertThat( parse( "${this is {the expression} literal" ) )
				.containsExactly( new Expression( "this is {the expression" ), new Literal( " literal" ) );
		assertThat( parse( "${this is {the expression\\} literal}" ) )
				.containsExactly( new Expression( "this is {the expression} literal" ) );

	}

	// parse choice format
	// parse expression
	// parse message lookup

	private List<MessageToken> parse( String message ) {
		return new MessageTokenCollector( message ).getTokens();
	}
}
