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
import com.foreach.across.modules.web.ui.ViewElements;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestViewElements
{
	private ViewElements list;

	private final SimpleElement one = new SimpleElement( "one" );
	private final SimpleElement two = new SimpleElement( "two" );
	private final SimpleElement three = new SimpleElement( "three" );
	private final SimpleElement four = new SimpleElement( "four" );

	private ContainerViewElement groupWithThree;

	@Before
	public void before() {
		list = new ViewElements();
		groupWithThree = new ContainerViewElement();
		groupWithThree.setName( "groupWithThree" );

		groupWithThree.add( three );
	}

	@Test
	public void emptyCollection() {
		assertTrue( list.isEmpty() );
		assertEquals( 0, list.size() );
		assertFalse( list.iterator().hasNext() );
	}

	@Test
	public void simpleAdd() {
		list.add( one );

		assertFalse( list.isEmpty() );
		assertEquals( 1, list.size() );
		assertSame( one, list.iterator().next() );

		list.add( two );
		assertFalse( list.isEmpty() );
		assertEquals( 2, list.size() );
		Iterator it = list.iterator();
		assertSame( one, it.next() );
		assertSame( two, it.next() );
	}

	@Test
	public void clear() {
		list.add( one );
		list.add( two );

		list.clear();

		assertTrue( list.isEmpty() );
	}

	@Test
	public void getByName() {
		list.add( one );
		list.add( three );

		assertTrue( list.contains( "one" ) );
		assertTrue( list.contains( "three" ) );
		assertFalse( list.contains( "two" ) );

		assertSame( one, list.get( "one" ) );
		assertSame( three, list.get( "three" ) );
		assertNull( list.get( "two" ) );
	}

	@Test
	public void getByNameFromGroup() {
		list.add( one );
		list.add( groupWithThree );

		assertTrue( list.contains( "one" ) );
		assertTrue( list.contains( "three" ) );
		assertTrue( list.contains( "groupWithThree" ) );

		assertSame( one, list.get( "one" ) );
		assertSame( groupWithThree, list.get( "groupWithThree" ) );
		assertSame( three, list.get( "three" ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingWithSameNameThrowsException() {
		list.add( one );
		list.add( new SimpleElement( "one" ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingWithSameNameInGroupThrowsException() {
		list.add( one );
		list.add( groupWithThree );
		list.add( three );
	}

	@Test
	public void parentForDirectChild() {
		list.add( one );
		list.add( two );

		assertSame( list, list.getParent( "one" ) );
		assertSame( list, list.getParent( "two" ) );
	}

	@Test
	public void parentForGroupMember() {
		list.add( groupWithThree );

		assertSame( list, list.getParent( "groupWithThree" ) );
		assertSame( groupWithThree, list.getParent( "three" ) );
	}

	@Test
	public void removeDirectChild() {
		list.add( one );

		assertSame( one, list.remove( "one" ) );
		assertTrue( list.isEmpty() );
	}

	@Test
	public void removeFromGroup() {
		list.add( one );
		list.add( groupWithThree );

		assertSame( three, list.remove( "three" ) );
		assertTrue( groupWithThree.isEmpty() );
	}

	@Test
	public void sortPropertiesInGroups() {
		ContainerViewElement group = new ContainerViewElement();
		group.setName( "group" );
		group.add( four );
		group.add( three );

		list.add( group );
		list.add( two );
		list.add( one );

		list.sort( "one", "two", "three", "four", "group" );

		Iterator it = list.iterator();
		assertSame( one, it.next() );
		assertSame( two, it.next() );
		assertSame( group, it.next() );
		assertFalse( it.hasNext() );

		it = group.iterator();
		assertSame( three, it.next() );
		assertSame( four, it.next() );
		assertFalse( it.hasNext() );
	}

	static class SimpleElement implements ViewElement
	{
		private String name;

		SimpleElement( String name ) {
			this.name = name;
		}

		@Override
		public String getElementType() {
			return name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getCustomTemplate() {
			return null;
		}
	}
}
