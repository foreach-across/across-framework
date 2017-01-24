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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestContainerViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void childElements() {
		ContainerViewElement container = new ContainerViewElement();

		assertFalse( container.hasChildren() );
		assertTrue( container.getChildren().isEmpty() );

		ViewElement child = mock( ViewElement.class );
		container.addChild( child );
		assertTrue( container.hasChildren() );
		assertEquals( 1, container.getChildren().size() );
		assertTrue( container.getChildren().contains( child ) );

		container.removeChild( child );
		assertFalse( container.hasChildren() );
		assertTrue( container.getChildren().isEmpty() );

		container.addChild( child );

		ViewElement other = mock( ViewElement.class );
		ViewElement third = mock( ViewElement.class );

		container.addChild( other );
		assertSame( container.getChildren().get( 0 ), child );
		assertSame( container.getChildren().get( 1 ), other );

		container.addFirstChild( third );
		assertSame( container.getChildren().get( 0 ), third );
		assertSame( container.getChildren().get( 1 ), child );
		assertSame( container.getChildren().get( 2 ), other );
	}

	@Test
	public void addAndClearChildren() {
		ContainerViewElement container = new ContainerViewElement();
		ViewElement one = mock( ViewElement.class );
		ViewElement two = mock( ViewElement.class );
		container.addChildren( Arrays.asList( one, two ) );

		assertSame( container.getChildren().get( 0 ), one );
		assertSame( container.getChildren().get( 1 ), two );

		container.clearChildren();
		assertFalse( container.hasChildren() );
		assertTrue( container.getChildren().isEmpty() );
	}

	@Test
	public void multipleTextElements() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TextViewElement( "one, " ) );
		container.addChild( new TextViewElement( "two, " ) );
		container.addChild( new TextViewElement( "three" ) );

		renderAndExpect( container, "one, two, three" );
	}

	@Test
	public void nestedContainerElements() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TextViewElement( "one, " ) );

		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( new TextViewElement( "two, " ) );
		subContainer.addChild( new TextViewElement( "three" ) );

		container.addChild( subContainer );
		renderAndExpect( container, "one, two, three" );
	}

	@Test
	public void customTemplateWithoutNesting() {
		ContainerViewElement container = new ContainerViewElement();
		container.setCustomTemplate( "th/test/elements/container" );
		container.addChild( new TextViewElement( "one" ) );
		container.addChild( new TextViewElement( "two" ) );
		container.addChild( new TextViewElement( "three" ) );

		renderAndExpect( container, "<ul><li>one</li><li>two</li><li>three</li></ul>" );
	}

	@Test
	public void customTemplateWithNesting() {
		ContainerViewElement container = new ContainerViewElement();
		container.setCustomTemplate( "th/test/elements/container :: nested(${component})" );
		container.addChild( new TextViewElement( "one" ) );

		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( new TextViewElement( "two, " ) );
		subContainer.addChild( new TextViewElement( "three" ) );

		ContainerViewElement otherSubContainer = new ContainerViewElement();
		otherSubContainer.setCustomTemplate( "th/test/elements/container" );
		otherSubContainer.addChild( new TextViewElement( "four" ) );
		otherSubContainer.addChild( new TextViewElement( "five" ) );

		subContainer.addChild( otherSubContainer );

		container.addChild( subContainer );

		renderAndExpect( container,
		                 "<ol><li>one</li><li>two, three<ul><li>four</li><li>five</li></ul></li></ol>" );
	}

	@Test
	public void customTemplateChild() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TemplateViewElement( CUSTOM_TEMPLATE ) );

		renderAndExpect( container, CUSTOM_TEMPLATE_OUTPUT );
	}
}
