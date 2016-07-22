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
package com.foreach.across.test.modules.web.ui;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils.find;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestContainerViewElementUtils
{
	private ContainerViewElement container;

	@Before
	public void before() {
		container = new ContainerViewElement();
	}

	@Test
	public void elementWithNameNotFound() {
		assertEquals( Optional.empty(), find( container, "notFound" ) );
	}

	@Test
	public void elementWithNameIsDirectChild() {
		TextViewElement text = new TextViewElement( "name", "text" );
		container.addChild( text );

		assertEquals( Optional.of( text ), find( container, "name" ) );
	}

	@Test
	public void elementWithNameIsInNestedContainer() {
		TextViewElement text = new TextViewElement( "name", "text" );
		NodeViewElement node = new NodeViewElement( "n" );
		node.addChild( text );
		container.addChild( node );

		assertEquals( Optional.of( text ), find( container, "name" ) );
	}

	@Test
	public void firstElementMatchingIsReturned() {
		TextViewElement text = new TextViewElement( "name", "text" );
		TextViewElement other = new TextViewElement( "name", "other text" );
		NodeViewElement node = new NodeViewElement( "n" );
		node.addChild( text );
		container.addChild( node );
		container.addChild( other );

		assertEquals( Optional.of( text ), find( container, "name" ) );

		Collections.reverse( container.getChildren() );

		assertEquals( Optional.of( other ), find( container, "name" ) );
	}

	@Test
	public void elementWithNameMustBeOfRequiredType() {
		TextViewElement text = new TextViewElement( "name", "text" );
		NodeViewElement node = new NodeViewElement( "parent", "n" );
		NodeViewElement other = new NodeViewElement( "name", "sub" );
		node.addChild( other );
		container.addChild( text );
		container.addChild( node );

		assertEquals( Optional.of( text ), find( container, "name", TextViewElement.class ) );
		assertEquals( Optional.of( other ), find( container, "name", ContainerViewElement.class ) );
		assertEquals( Optional.of( node ), find( container, "parent", NodeViewElement.class ) );
		assertEquals( Optional.empty(), find( container, "parent", TextViewElement.class ) );
	}

	@Test
	public void findParentOfElementNotFound() {
		assertEquals( Optional.empty(), ContainerViewElementUtils.findParent( container, mock( ViewElement.class ) ) );
	}

	@Test
	public void parentIsSelfOfDirectChild() {
		ViewElement e = mock( ViewElement.class );
		container.addChild( mock( ViewElement.class ) );
		container.addChild( e );

		assertEquals( Optional.of( container ), ContainerViewElementUtils.findParent( container, e ) );
	}

	@Test
	public void findParentTraversalIsInTopDownOrder() {
		ViewElement e = mock( ViewElement.class );

		ContainerViewElement group = new ContainerViewElement();
		container.addChild( group );
		container.addChild( e );

		group.addChild( e );

		assertEquals( Optional.of( group ), ContainerViewElementUtils.findParent( container, e ) );
	}

	@Test
	public void sortingUnnamedElementsDoesNotModify() {
		ViewElement one = mock( ViewElement.class );
		ContainerViewElement two = new ContainerViewElement();

		ViewElement subOne = mock( ViewElement.class );
		ViewElement subTwo = mock( ViewElement.class );
		two.addChild( subTwo );
		two.addChild( subOne );

		List<ViewElement> original = Collections.unmodifiableList( Arrays.asList( two, one ) );

		List<ViewElement> sortable = new ArrayList<>( original );
		ContainerViewElementUtils.sort( sortable, "one", "two" );
		assertEquals( original, sortable );
		assertEquals( two.getChildren(), ( (ContainerViewElement) sortable.get( 0 ) ).getChildren() );

		ContainerViewElement container = new ContainerViewElement();
		container.getChildren().addAll( original );
		ContainerViewElementUtils.sort( container, "one", "two" );
		assertEquals( original, container.getChildren() );
		assertEquals( two.getChildren(), ( (ContainerViewElement) container.getChildren().get( 0 ) ).getChildren() );
	}

	@Test
	public void namedElementsAreSortedBeforeUnnamed() {
		TextViewElement one = new TextViewElement( "one", "one" );

		ViewElement three = mock( ViewElement.class );
		ContainerViewElement two = new ContainerViewElement( "two" );

		ViewElement subOne = new TextViewElement( "one" );
		ViewElement subTwo = new TextViewElement( "two" );
		two.addChild( subTwo );
		two.addChild( subOne );

		List<ViewElement> original = Collections.unmodifiableList( Arrays.asList( three, two, one ) );

		List<ViewElement> sortable = new ArrayList<>( original );
		ContainerViewElementUtils.sort( sortable, "one", "two" );
		assertEquals( Arrays.asList( one, two, three ), sortable );
		assertEquals( two.getChildren(), ( (ContainerViewElement) sortable.get( 1 ) ).getChildren() );

		ContainerViewElement container = new ContainerViewElement();
		container.getChildren().addAll( original );
		ContainerViewElementUtils.sort( container, "one", "two" );
		assertEquals( Arrays.asList( one, two, three ), container.getChildren() );
		assertEquals( two.getChildren(), ( (ContainerViewElement) container.getChildren().get( 1 ) ).getChildren() );
	}

	@Test
	public void sortingRecursively() {
		TextViewElement one = new TextViewElement( "one", "one" );

		ViewElement three = mock( ViewElement.class );
		ContainerViewElement two = new ContainerViewElement( "two" );

		ViewElement subOne = new TextViewElement( "one", "subOne" );
		ViewElement subTwo = new TextViewElement( "two", "subTwo" );
		two.addChild( subTwo );
		two.addChild( subOne );

		List<ViewElement> original = Collections.unmodifiableList( Arrays.asList( three, two, one ) );

		List<ViewElement> sortable = new ArrayList<>( original );
		ContainerViewElementUtils.sortRecursively( sortable, "one", "two" );
		assertEquals( Arrays.asList( one, two, three ), sortable );
		assertEquals( Arrays.asList( subOne, subTwo ), ( (ContainerViewElement) sortable.get( 1 ) ).getChildren() );

		ContainerViewElement container = new ContainerViewElement();
		container.getChildren().addAll( original );
		ContainerViewElementUtils.sortRecursively( container, "one", "two" );
		assertEquals( Arrays.asList( one, two, three ), container.getChildren() );
		assertEquals(
				Arrays.asList( subOne, subTwo ),
				( (ContainerViewElement) container.getChildren().get( 1 ) ).getChildren()
		);
	}

	@Test
	public void removeElement() {
		ContainerViewElement container = new ContainerViewElement();
		TextViewElement one = new TextViewElement( "one", "one" );
		ContainerViewElement two = new ContainerViewElement( "two" );
		TextViewElement subOne = new TextViewElement( "subOne", "subOne" );
		two.addChild( subOne );

		two.addChild( new TextViewElement( "subTwo", "subTwo" ) );

		container.addChild( one );
		container.addChild( two );

		assertTrue( ContainerViewElementUtils.remove( container, subOne ) );
		assertFalse( ContainerViewElementUtils.remove( container, subOne ) );

		assertEquals( 1, two.getChildren().size() );
	}

	@Test
	public void removeElementByName() {
		ContainerViewElement container = new ContainerViewElement();
		TextViewElement one = new TextViewElement( "one", "one" );
		ContainerViewElement two = new ContainerViewElement( "two" );
		TextViewElement subOne = new TextViewElement( "subOne", "subOne" );
		two.addChild( subOne );

		two.addChild( new TextViewElement( "subTwo", "subTwo" ) );

		container.addChild( one );
		container.addChild( two );

		assertEquals( Optional.of( subOne ), ContainerViewElementUtils.remove( container, "subOne" ) );
		assertEquals( Optional.empty(), ContainerViewElementUtils.remove( container, "subOne" ) );

		assertEquals( 1, two.getChildren().size() );
	}

	@Test
	public void moveReturnsFalseIfElementDoesNotExist() {
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement parent = new ContainerViewElement( "parent" );
		container.addChild( parent );

		assertFalse( ContainerViewElementUtils.move( container, "unexisting", parent ) );
	}

	@Test
	public void moveReturnsFalseIfParentDoesNotExistOrNotAContainer() {
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement parent = new ContainerViewElement( "parent" );
		container.addChild( parent );
		container.addChild( new TextViewElement( "text", "text" ) );

		assertFalse( ContainerViewElementUtils.move( container, new TextViewElement( "moveme" ), "badparent" ) );
		assertFalse( ContainerViewElementUtils.move( container, new TextViewElement( "moveme" ), "text" ) );
	}

	@Test
	public void moveToCurrentParentReturnsFalseIfNotRemovedFromContainer() {
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement parent = new ContainerViewElement( "parent" );
		container.addChild( parent );
		container.addChild( new TextViewElement( "text", "text" ) );

		assertFalse( ContainerViewElementUtils.move( container, parent, container ) );
		assertSame( parent, container.getChildren().get( 0 ) );

		assertFalse( ContainerViewElementUtils.move( container, "text", container ) );
	}

	@Test
	public void moveReturnsTrueIfNewParentAlreadyContainsButElementIsRemovedFromContainerParent() {
		ContainerViewElement parent = new ContainerViewElement( "parent" );
		TextViewElement text = new TextViewElement( "text", "text" );
		parent.addChild( text );
		parent.addChild( new TextViewElement( "other", "other" ) );

		ContainerViewElement container = new ContainerViewElement();
		container.addChild( text );

		assertTrue( ContainerViewElementUtils.move( container, text, parent ) );
		assertSame( text, parent.getChildren().get( 0 ) );
		assertFalse( container.hasChildren() );
	}

	@Test
	public void moveToDifferentParent() {
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement parent = new ContainerViewElement( "parent" );
		container.addChild( parent );

		TextViewElement text = new TextViewElement( "text", "text" );
		container.addChild( text );

		assertTrue( ContainerViewElementUtils.move( container, text, parent ) );

		assertSame( parent, container.getChildren().get( 0 ) );
		assertFalse( container.getChildren().contains( text ) );
		assertSame( text, parent.getChildren().get( 0 ) );
	}

	@Test
	public void moveToDifferentParentByName() {
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement parent = new ContainerViewElement( "parent" );
		container.addChild( parent );

		TextViewElement text = new TextViewElement( "text", "text" );
		container.addChild( text );

		assertTrue( ContainerViewElementUtils.move( container, "text", "parent" ) );

		assertSame( parent, container.getChildren().get( 0 ) );
		assertFalse( container.getChildren().contains( text ) );
		assertSame( text, parent.getChildren().get( 0 ) );
	}

	@Test
	public void replaceIsFalseIfElementNotInContainer() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( new TextViewElement() );

		TextViewElement other = new TextViewElement( "other", "other" );
		assertFalse( ContainerViewElementUtils.replace( container, other, new ContainerViewElement() ) );

		assertTrue( container.getChildren().get( 0 ) instanceof TextViewElement );
	}

	@Test
	public void replaceIsRemoveIfReplacementIsNull() {
		TextViewElement text = new TextViewElement( "text", "text" );
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( text );
		container.addChild( subContainer );

		assertTrue( ContainerViewElementUtils.replace( container, text, null ) );

		assertFalse( subContainer.hasChildren() );
		assertSame( subContainer, container.getChildren().get( 0 ) );
	}

	@Test
	public void replaceOfSpecificElement() {
		TextViewElement text = new TextViewElement( "text", "text" );
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( new ContainerViewElement() );
		subContainer.addChild( text );
		container.addChild( subContainer );

		TextViewElement other = new TextViewElement( "other", "other" );
		assertTrue( ContainerViewElementUtils.replace( container, text, other ) );

		assertSame( other, subContainer.getChildren().get( 1 ) );
		assertFalse( subContainer.getChildren().contains( text ) );
	}

	@Test
	public void replaceIsFalseIfElementIsNotOfRequiredType() {
		TextViewElement text = new TextViewElement( "text", "text" );
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( new ContainerViewElement() );
		subContainer.addChild( text );
		container.addChild( subContainer );

		assertFalse( ContainerViewElementUtils.replace( container, "text", ContainerViewElement.class, e -> null ) );
		assertSame( text, subContainer.getChildren().get( 1 ) );
	}

	@Test
	public void replaceOfTypedElement() {
		TextViewElement text = new TextViewElement( "text", "text" );
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( new ContainerViewElement() );
		subContainer.addChild( text );
		container.addChild( subContainer );

		ContainerViewElement replacement = new ContainerViewElement();

		assertTrue( ContainerViewElementUtils.replace( container, "text", TextViewElement.class, t -> {
			replacement.addChild( t );
			return replacement;
		} ) );

		assertSame( replacement, subContainer.getChildren().get( 1 ) );
		assertSame( text, replacement.getChildren().get( 0 ) );
	}

	@Test
	public void replaceWithNullReturningFunctionIsRemove() {
		TextViewElement text = new TextViewElement( "text", "text" );
		ContainerViewElement container = new ContainerViewElement();
		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.addChild( text );
		container.addChild( subContainer );

		assertTrue( ContainerViewElementUtils.replace( container, "text", e -> null ) );
		assertFalse( subContainer.hasChildren() );
	}
}
