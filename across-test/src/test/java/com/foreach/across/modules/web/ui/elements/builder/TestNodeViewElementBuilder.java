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

import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.junit.Test;

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
	public void addElements() {
		TextViewElement textOne = new TextViewElement( "textOne", "text 1" );
		TextViewElement textTwo = new TextViewElement( "textTwo", "text 2" );

		builder.tagName( "a" ).attribute( "href", "somelink" ).removeAttribute( "class" ).add( textOne )
		       .addFirst( textTwo );

		build();

		assertEquals( 2, element.getChildren().size() );
		assertSame( textTwo, element.getChildren().get( 0 ) );
		assertSame( textOne, element.getChildren().get( 1 ) );

		assertEquals( "a", element.getTagName() );
		assertEquals( "somelink", element.getAttribute( "href" ) );
		assertNull( element.getAttribute( "class" ) );
		assertTrue( element.hasAttribute( "class" ) );
	}
}
