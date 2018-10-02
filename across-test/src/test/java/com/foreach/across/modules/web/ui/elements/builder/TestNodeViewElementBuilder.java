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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class TestNodeViewElementBuilder extends AbstractViewElementBuilderTest<NodeViewElementBuilder, NodeViewElement>
{
	@Override
	protected NodeViewElementBuilder createBuilder( ViewElementBuilderFactory factory ) {
		return new NodeViewElementBuilder( "div" );
	}

	@Test
	public void defaults() {
		build();

		assertFalse( element.hasChildren() );
	}

	@Test
	public void builderMethodsAreCorrect() {
		String htmlId = RandomStringUtils.random( 200 );
		builder.htmlId( htmlId ).tagName( "tagName" ).css( "one", "two", "three" ).removeCss( "three" )
		       .data( "number", 53 )
		       .data( "index", 44 )
		       .attribute( "foo", "bar" )
		       .attributes( Collections.singletonMap( "wonderful", "world" ) )
		       .removeData( "index" );
		build();
		assertEquals( htmlId, element.getHtmlId() );
		assertEquals( "tagName", element.getTagName() );
		assertEquals( "one two", element.getAttributes().get( "class" ) );
		assertEquals( 53, element.getAttributes().get( "data-number" ) );
		assertNull( element.getAttributes().get( "data-index" ) );
		assertEquals( "bar", element.getAttributes().get( "foo" ) );
		assertEquals( "world", element.getAttributes().get( "wonderful" ) );
	}

	@Test
	public void builderMethodClears() {
		builder.attribute( "one", 1 ).attribute( "two", 2 );
		builder.clearAttributes();
		build();
		assertEquals( 0, element.getAttributes().size() );
	}

	@Test
	public void nullElementsAreSimplyIgnored() {
		builder.add( (ViewElement) null )
		       .add( (ViewElementBuilder) null )
		       .add( null, (ViewElement) null )
		       .addAll( Arrays.asList( null, null ) )
		       .addFirst( (ViewElement) null )
		       .addFirst( (ViewElementBuilder) null )
		       .configure( null )
		       .configure( container -> container.add( (ViewElement) null ) );

		build();

		assertFalse( element.hasChildren() );
	}

	@Test
	public void configure() {
		TextViewElement textOne = new TextViewElement( "textOne", "text 1" );
		TextViewElement textTwo = new TextViewElement( "textTwo", "text 2" );

		builder.configure( node -> node.tagName( "b" ).add( textTwo ) ).add( textOne );
		build();

		assertEquals( 2, element.getChildren().size() );
		assertSame( textTwo, element.getChildren().get( 0 ) );
		assertSame( textOne, element.getChildren().get( 1 ) );
		assertEquals( "b", element.getTagName() );
	}

	@Test
	public void addElements() {
		TextViewElement textOne = new TextViewElement( "textOne", "text 1" );
		TextViewElement textTwo = new TextViewElement( "textTwo", "text 2" );

		builder.tagName( "a" )
		       .attribute( "href", "somelink" )
		       .data( "role", "link" )
		       .removeAttribute( "class" )
		       .add( textOne )
		       .addFirst( textTwo );

		build();

		assertEquals( 2, element.getChildren().size() );
		assertSame( textTwo, element.getChildren().get( 0 ) );
		assertSame( textOne, element.getChildren().get( 1 ) );

		assertEquals( "a", element.getTagName() );
		assertEquals( "somelink", element.getAttribute( "href" ) );
		assertEquals( "link", element.getAttribute( "data-role" ) );
		assertNull( element.getAttribute( "class" ) );
		assertTrue( element.hasAttribute( "class" ) );
	}
}
