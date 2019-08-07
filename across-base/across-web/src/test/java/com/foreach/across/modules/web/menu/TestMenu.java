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

package com.foreach.across.modules.web.menu;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TestMenu
{
	@Test
	public void parentAndRootValidation() {
		Menu menu = new Menu();
		assertFalse( menu.hasParent() );
		assertTrue( menu.isRoot() );

		Menu sub = menu.addItem( "sub" );
		assertFalse( menu.hasParent() );
		assertTrue( menu.isRoot() );
		assertTrue( sub.hasParent() );
		assertFalse( sub.isRoot() );
		assertSame( menu, sub.getParent() );
		assertSame( menu, sub.getRoot() );

		Menu subsub = sub.addItem( "subsub" );
		assertFalse( menu.hasParent() );
		assertTrue( menu.isRoot() );
		assertTrue( sub.hasParent() );
		assertFalse( sub.isRoot() );
		assertSame( menu, sub.getParent() );
		assertSame( menu, sub.getRoot() );
		assertTrue( subsub.hasParent() );
		assertFalse( subsub.isRoot() );
		assertSame( sub, subsub.getParent() );
		assertSame( menu, sub.getRoot() );
	}

	@Test
	public void menuLevelsAreDynamicallyCalculated() {
		Menu menu = new Menu( "", "" );
		assertEquals( Menu.ROOT_LEVEL, menu.getLevel() );

		Menu item1 = menu.addItem( "path1" );
		Menu item2 = menu.addItem( "path2" );
		Menu subItem1 = item2.addItem( "sub1" );
		Menu subItem2 = item2.addItem( "sub2" );
		Menu subSubItem1 = subItem1.addItem( "subsub1" );
		Menu subSubItem2 = subItem2.addItem( "subsub2" );

		assertEquals( 1, item1.getLevel() );
		assertEquals( 1, item2.getLevel() );
		assertEquals( 2, subItem1.getLevel() );
		assertEquals( 2, subItem2.getLevel() );
		assertEquals( 3, subSubItem1.getLevel() );
		assertEquals( 3, subSubItem2.getLevel() );

		Menu newRoot = new Menu();
		newRoot.addItem( menu );
		assertEquals( Menu.ROOT_LEVEL, newRoot.getLevel() );
		assertEquals( 1, menu.getLevel() );
		assertEquals( 2, item1.getLevel() );
		assertEquals( 2, item2.getLevel() );
		assertEquals( 3, subItem1.getLevel() );
		assertEquals( 3, subItem2.getLevel() );
		assertEquals( 4, subSubItem1.getLevel() );
		assertEquals( 4, subSubItem2.getLevel() );
	}

	@Test
	public void sameMenuCanOnlyBelongToSingleMenu() {
		Menu menu = new Menu();

		Menu item = new Menu( "path" );
		assertFalse( item.hasParent() );
		assertNull( item.getParent() );

		menu.addItem( item );
		assertTrue( item.hasParent() );
		assertSame( menu, item.getParent() );

		Menu otherMenu = new Menu();
		boolean failed = false;

		try {
			otherMenu.addItem( item );
		}
		catch ( RuntimeException rte ) {
			failed = true;
		}

		assertTrue( failed );
	}

	@Test
	public void removedMenuCanBeAddedToAnotherMenu() {
		Menu menu = new Menu();
		Menu item = new Menu( "sub" );
		Menu subSub = new Menu( "subsub" );

		menu.addItem( item );
		item.addItem( subSub );

		Menu otherMenu = new Menu();
		boolean failed = false;

		try {
			otherMenu.addItem( subSub );
		}
		catch ( RuntimeException rte ) {
			failed = true;
		}

		assertTrue( failed );

		menu.remove( subSub );
		otherMenu.addItem( subSub );

		assertTrue( subSub.hasParent() );
		assertSame( otherMenu, subSub.getParent() );
	}

	@Test
	public void childItemMeansParentIsSelected() {
		Menu menu = new Menu();
		menu.addItem( "path1" );

		Menu item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		Menu subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		assertFalse( menu.getItem( MenuSelector.byPath( "path1" ) ).isSelected() );
		assertTrue( menu.getItemWithPath( "path2" ).isSelected() );
		assertFalse( menu.getItemWithPath( "sub1" ).isSelected() );
		assertTrue( menu.getItemWithPath( "sub2" ).isSelected() );
		assertTrue( menu.getItemWithPath( "subsub1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub2" ).isSelected() );
	}

	@Test
	public void deselectingParentDeselectsChild() {
		Menu menu = new Menu();
		menu.addItem( "path1" );

		Menu item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		Menu subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		item.setSelected( false );

		assertFalse( menu.getItemWithPath( "path1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "path2" ).isSelected() );
		assertFalse( menu.getItemWithPath( "sub1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "sub2" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub2" ).isSelected() );
	}

	@Test
	public void deselectingChildDoesNotDeselectParent() {
		Menu menu = new Menu();
		menu.addItem( "path1" );

		Menu item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		Menu subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		subItem.getItemWithPath( "subsub1" ).setSelected( false );

		assertFalse( menu.getItemWithPath( "path1" ).isSelected() );
		assertTrue( menu.getItemWithPath( "path2" ).isSelected() );
		assertFalse( menu.getItemWithPath( "sub1" ).isSelected() );
		assertTrue( menu.getItemWithPath( "sub2" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub2" ).isSelected() );
	}

	@Test
	public void onlyOneItemCanBeSelected() {
		Menu menu = new Menu();
		menu.addItem( "path1" );

		Menu item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		Menu subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		menu.getItemWithPath( "sub1" ).setSelected( true );

		assertFalse( menu.getItemWithPath( "path1" ).isSelected() );
		assertTrue( menu.getItemWithPath( "path2" ).isSelected() );
		assertTrue( menu.getItemWithPath( "sub1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "sub2" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub1" ).isSelected() );
		assertFalse( menu.getItemWithPath( "subsub2" ).isSelected() );
	}

	@Test
	public void selectedItemIsAlwaysLookedForInTheSubtree() {
		Menu menu = new Menu();
		menu.addItem( "path1" );

		Menu item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		Menu subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		Menu selected = menu.getSelectedItem();
		assertSame( item, selected );

		selected = menu.getLowestSelectedItem();
		assertSame( menu.getItemWithPath( "subsub1" ), selected );
		assertNull( menu.getItemWithPath( "path1" ).getSelectedItem() );

		subItem.setSelected( true );

		assertSame( item, menu.getSelectedItem() );
		assertSame( subItem, menu.getLowestSelectedItem() );
		assertNull( subItem.getSelectedItem() );
	}

	@Test
	public void selectedItemPathIfNoneSelected() {
		Menu menu = new Menu();
		menu.addItem( "one" );

		assertNull( menu.getSelectedItem() );
		assertTrue( menu.getSelectedItemPath().isEmpty() );
	}

	@Test
	public void selectedItemPathIfRootSelected() {
		Menu menu = new Menu();
		menu.addItem( "one" );

		menu.setSelected( true );

		assertNull( menu.getSelectedItem() );
		List<Menu> items = menu.getSelectedItemPath();
		assertEquals( 1, items.size() );
		assertSame( menu, items.get( 0 ) );
	}

	@Test
	public void selectedItemPathIfLowerSelected() {
		Menu menu = new Menu();
		Menu item = menu.addItem( "one" );
		item.addItem( "sub-one" );
		Menu subTwo = item.addItem( "sub-two" );

		subTwo.setSelected( true );

		List<Menu> items = menu.getSelectedItemPath();
		assertEquals( 3, items.size() );
		assertSame( menu, items.get( 0 ) );
		assertSame( item, items.get( 1 ) );
		assertSame( subTwo, items.get( 2 ) );

		items = item.getSelectedItemPath();
		assertEquals( 2, items.size() );
		assertSame( item, items.get( 0 ) );
		assertSame( subTwo, items.get( 1 ) );
	}

	@Test
	public void mergeMenu() {
		@SuppressWarnings("unchecked")
		Comparator<Menu> mockComparator = mock( Comparator.class );

		Menu menu = new Menu();
		menu.addItem( "path1" );

		Menu item = menu.addItem( "path2" );
		item.setAttribute( "myattribute", "myvalue" );
		item.setAttribute( "myattribute2", "myvalue" );

		Menu other = new Menu();
		other.setPath( "other" );
		other.setTitle( "title other" );
		other.setDisabled( true );
		other.setGroup( true );
		other.setAttribute( "myattribute", "myothervalue" );
		other.setAttribute( "myotherattribute", "myothervalue" );

		Menu otherItem = other.addItem( "otherItem" );
		otherItem.setSelected( true );
		otherItem.setUrl( "other item url" );
		otherItem.setComparator( mockComparator, true );

		// Merge into sub-tree
		item.merge( other, false );

		assertEquals( 2, menu.size() );
		assertEquals( "path1", menu.getFirstItem().getPath() );

		Menu modified = menu.getItems().get( 1 );
		assertEquals( "other", modified.getPath() );
		assertEquals( "title other", modified.getTitle() );
		assertTrue( modified.isDisabled() );
		assertTrue( modified.isGroup() );
		assertEquals( 3, modified.getAttributes().size() );
		assertEquals( "myothervalue", modified.getAttribute( "myattribute" ) );
		assertEquals( "myvalue", modified.getAttribute( "myattribute2" ) );
		assertEquals( "myothervalue", modified.getAttribute( "myotherattribute" ) );
		assertTrue( modified.isSelected() );

		assertEquals( 1, modified.size() );
		modified = modified.getFirstItem();
		assertEquals( "otherItem", modified.getPath() );
		assertEquals( "other item url", modified.getUrl() );
		assertTrue( modified.isSelected() );
		assertSame( mockComparator, modified.getComparator() );
		assertTrue( modified.isComparatorInheritable() );
	}

	@Test
	public void menuHasUrlIfUrlIsNotEmpty() {
		Menu menu = new Menu( "/path" );
		assertEquals( "/path", menu.getUrl() );
		assertFalse( menu.hasUrl() );
		menu.setUrl( "" );
		assertFalse( menu.hasUrl() );
		assertEquals( "/path", menu.getUrl() );
		menu.setUrl( " " );
		assertTrue( menu.hasUrl() );
		assertEquals( " ", menu.getUrl() );
	}
}
