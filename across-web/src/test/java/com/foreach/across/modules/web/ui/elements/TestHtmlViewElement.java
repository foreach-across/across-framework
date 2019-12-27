/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.modules.web.ui.elements;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.foreach.across.modules.web.ui.MutableViewElement.Functions.elementName;
import static com.foreach.across.modules.web.ui.ViewElement.predicateFor;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElement.Functions.*;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("Test HTML wither functions")
class TestHtmlViewElement
{
	private NodeViewElement node = new NodeViewElement( "div" );

	@Test
	void cssWither() {
		node.set( css( "x", "y", "z" ), elementName( "myNode" ) );

		assertThat( node.getName() ).isEqualTo( "myNode" );

		assertThat( node.hasCssClass( "x" ) ).isTrue();
		assertThat( node.hasCssClass( "y" ) ).isTrue();
		assertThat( node.hasCssClass( "z" ) ).isTrue();

		assertThat( node.matches( css( "a" ) ) ).isFalse();
		assertThat( node.matches( css( "x", "y", "z" ) ) ).isTrue();
		assertThat( node.matches( css( "x" ).and( css( "y" ) ) ) ).isTrue();

		node.remove( css( "y", "z" ) );

		assertThat( node.hasCssClass( "x" ) ).isTrue();
		assertThat( node.hasCssClass( "y" ) ).isFalse();
		assertThat( node.hasCssClass( "z" ) ).isFalse();

		assertThat( node.matches( css( "x" ) ) ).isTrue();
		assertThat( node.matches( css( "x" ).and( css( "y" ) ) ) ).isFalse();
		assertThat( node.matches( css( "x" ).or( css( "y" ) ) ) ).isTrue();
	}

	@Test
	void attributeWithers() {
		node.set( attribute( "key1", "val1" ),
		          attribute( "key2" ).withValue( "val2" ),
		          data( "key3", "val3" ),
		          data( "key4" ).withValue( "val4" ),
		          aria( "key5", "val5" ),
		          aria( "key6" ).withValue( "val6" ) );

		assertThat( node.getAttribute( "key1" ) )
				.isEqualTo( "val1" )
				.isEqualTo( node.get( attribute( "key1" ) ) )
				.isEqualTo( node.get( attribute( "key1" ).as( String.class ) ) );
		assertThat( node.getAttribute( "key2" ) ).isEqualTo( "val2" );

		assertThat( node.matches( attribute( "key1" ) ) ).isTrue();
		assertThat( node.matches( attribute( "key3" ) ) ).isFalse();
		assertThat( node.matches( data( "key3" ) ) ).isTrue();
		assertThat( node.matches( data( "key5" ) ) ).isFalse();
		assertThat( node.matches( aria( "key5" ) ) ).isTrue();
		assertThat( node.matches( aria( "key7" ) ) ).isFalse();

		assertThat( node.matches( attribute( "key2" ).withValue( "val2" ) ) ).isTrue();
		assertThat( node.matches( attribute( "key2", "val1" ) ) ).isFalse();
		assertThat( node.matches( data( "key3" ).withValue( "val3" ) ) ).isTrue();
		assertThat( node.matches( data( "key4", "val3" ) ) ).isFalse();
		assertThat( node.matches( aria( "key5" ).withValue( "val5" ) ) ).isTrue();
		assertThat( node.matches( aria( "key6", "val5" ) ) ).isFalse();

		assertThat( node.matches( predicateFor( NodeViewElement.class, node -> node.hasAttribute( "key2" ) ) ) ).isTrue();

		assertThat( node.getAttribute( "data-key3" ) )
				.isEqualTo( "val3" )
				.isEqualTo( node.get( data( "key3" ) ) )
				.isEqualTo( node.get( data( "key3" ).as( String.class ) ) );
		assertThat( node.getAttribute( "data-key4" ) ).isEqualTo( "val4" );

		assertThat( node.getAttribute( "aria-key5" ) )
				.isEqualTo( "val5" )
				.isEqualTo( node.get( aria( "key5" ) ) )
				.isEqualTo( node.get( aria( "key5" ).as( String.class ) ) );
		assertThat( node.getAttribute( "aria-key6" ) ).isEqualTo( "val6" );

		node.remove( attribute( "key1" ), data( "key3" ), aria( "key5" ) );

		assertThat( node.getAttributes().keySet() ).containsExactly( "key2", "data-key4", "aria-key6" );
	}

	@Test
	void childrenWither() {
		node.set( children( html.text( "one" ), html.div() ) );
		assertThat( node.getChildren().size() ).isEqualTo( 2 );

		assertThat( node.getChildren().get( 0 ) ).isInstanceOf( TextViewElement.class );
		assertThat( node.getChildren().get( 1 ) ).isInstanceOf( NodeViewElement.class );
	}

	@Test
	void tagNameWither() {
		node.set( tagName( "span" ) );
		assertThat( node.getTagName() ).isEqualTo( "span" );

		assertThat( html.br( tagName( "myTag" ) ).getTagName() ).isEqualTo( "myTag" );
	}

	@Test
	void htmlIdWither() {
		node.set( htmlId( "123" ) );
		assertThat( node.getHtmlId() ).isEqualTo( "123" );
	}

	@Test
	void textAddsAsChildOrText() {
		TextViewElement other = TextViewElement.html( "other text" );
		other.set( html.text( "added as property" ) );
		assertThat( other.getText() ).isEqualTo( "added as property" );

		node.set( html.text( "added as child" ) );
		assertThat( ( (TextViewElement) node.getChildren().get( 0 ) ).getText() ).isEqualTo( "added as child" );
	}
}
