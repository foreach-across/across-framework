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

import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;

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
	public void multipleTextElementsWithOnlyOneVisible() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TextViewElement( "one, " ) );

		TextViewElement visible = new TextViewElement( "two, " );
		visible.setName( "visible" );

		container.addChild( visible );
		container.addChild( new TextViewElement( "three" ) );

		renderAndExpect( container, model -> model.addAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT, "visible" ), "two, " );
	}

	@Test
	public void multipleTextElementsWithMultipleVisible() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TextViewElement( "one, " ) );

		TextViewElement visible = new TextViewElement( "two, " );
		visible.setName( "visible" );
		container.addChild( visible );

		TextViewElement alsoVisible = new TextViewElement( "three" );
		alsoVisible.setName( "visible" );
		container.addChild( alsoVisible );

		renderAndExpect( container, model -> model.addAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT, "visible" ), "two, three" );
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
	public void customTemplateWithNestingAndSelectedVisible() {
		ContainerViewElement container = new ContainerViewElement();
		container.setCustomTemplate( "th/test/elements/container :: nested(${component})" );
		container.addChild( new TextViewElement( "one" ) );

		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( new TextViewElement( "two, " ) );
		subContainer.addChild( new TextViewElement( "three" ) );

		ContainerViewElement otherSubContainer = new ContainerViewElement();
		otherSubContainer.setName( "visible" );
		otherSubContainer.setCustomTemplate( "th/test/elements/container" );
		otherSubContainer.addChild( new TextViewElement( "four" ) );
		otherSubContainer.addChild( new TextViewElement( "five" ) );

		subContainer.addChild( otherSubContainer );

		container.addChild( subContainer );

		renderAndExpect( container, model -> model.addAttribute( WebTemplateInterceptor.RENDER_VIEW_ELEMENT, "visible" ),
		                 "<ul><li>four</li><li>five</li></ul>" );
	}

	@Test
	public void moveElements() {
		ContainerViewElement source = new ContainerViewElement();
		ContainerViewElement members = new ContainerViewElement();
		members.addChild( new TextViewElement( "one", "one" ) );
		members.addChild( new TextViewElement( "three", "three" ) );
		ContainerViewElement subMembers = new ContainerViewElement();
		subMembers.addChild( new NodeViewElement( "two", "two" ) );
		members.addChild( subMembers );
		source.addChild( members );

		ContainerViewElement target = new ContainerViewElement();

		assertTrue( source.find( "one" ).isPresent() );
		assertTrue( source.find( "two" ).isPresent() );
		assertTrue( source.find( "three" ).isPresent() );

		source.removeAllFromTree( "one", "two", "four" )
		      .forEach( target::addChild );

		assertTrue( target.find( "one" ).isPresent() );
		assertTrue( target.find( "two" ).isPresent() );
		assertTrue( source.find( "three" ).isPresent() );
	}

	@Test
	public void customTemplateChild() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TemplateViewElement( CUSTOM_TEMPLATE ) );

		renderAndExpect( container, CUSTOM_TEMPLATE_OUTPUT );
	}

	@Test
	public void apply() {
		TextViewElement member = TextViewElement.text( "hello" );

		ContainerViewElement container = new ContainerViewElement();
		container.apply( c -> c.addChild( member ) );

		assertSame( member, container.getChildren().get( 0 ) );
	}

	@Test
	public void applyUnsafe() {
		Consumer<MutableViewElement> consumer = e -> e.setName( "containerName" );

		ContainerViewElement container = new ContainerViewElement();
		container.applyUnsafe( consumer );

		assertEquals( "containerName", container.getName() );
	}
}
