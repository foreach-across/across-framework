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

import com.foreach.across.modules.web.ui.ViewElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.foreach.across.modules.web.ui.MutableViewElement.Functions.elementName;
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

		node.remove( css( "y", "z" ) );

		assertThat( node.hasCssClass( "x" ) ).isTrue();
		assertThat( node.hasCssClass( "y" ) ).isFalse();
		assertThat( node.hasCssClass( "z" ) ).isFalse();
	}

	@Test
	void conditionalWithers() {
		boolean enabled = true;

		node.set( css( "x" ).ifTrue( enabled ), css( "y" ).ifFalse( enabled ) );
		assertThat( node.hasCssClass( "x" ) ).isTrue();
		assertThat( node.hasCssClass( "y" ) ).isFalse();

		AtomicBoolean ref = new AtomicBoolean();
		ViewElement.WitherSetter<HtmlViewElement> conditionalCss = css( "z" ).ifTrue( ref::get );
		node.set( conditionalCss );
		assertThat( node.hasCssClass( "z" ) ).isFalse();

		ref.set( true );

		node.set( conditionalCss );
		assertThat( node.hasCssClass( "z" ) ).isTrue();
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
