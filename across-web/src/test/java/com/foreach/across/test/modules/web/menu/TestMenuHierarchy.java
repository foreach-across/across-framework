package com.foreach.across.test.modules.web.menu;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuItem;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMenuHierarchy
{
	@Test
	public void parentAndRootValidation() {
		Menu menu = new Menu();
		assertFalse( menu.hasParent() );
		assertTrue( menu.isRoot() );

		MenuItem sub = menu.addItem( "sub" );
		assertFalse( menu.hasParent() );
		assertTrue( menu.isRoot() );
		assertTrue( sub.hasParent() );
		assertFalse( sub.isRoot() );
		assertSame( menu, sub.getParent() );
		assertSame( menu, sub.getRoot() );

		MenuItem subsub = sub.addItem( "subsub" );
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
	public void sameMenuItemCanOnlyBelongToSingleMenu() {
		Menu menu = new Menu();

		MenuItem item = new MenuItem( "path" );
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
	public void removedMenuItemCanBeAddedToAnotherMenu() {
		Menu menu = new Menu();
		MenuItem item = new MenuItem( "sub" );
		MenuItem subSub = new MenuItem( "subsub" );

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

		MenuItem item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		MenuItem subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		assertFalse( menu.getItemWithPath( "path1" ).isSelected() );
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

		MenuItem item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		MenuItem subItem = item.addItem( "sub2" );

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

		MenuItem item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		MenuItem subItem = item.addItem( "sub2" );

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

		MenuItem item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		MenuItem subItem = item.addItem( "sub2" );

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

		MenuItem item = menu.addItem( "path2" );
		item.addItem( "sub1" );
		MenuItem subItem = item.addItem( "sub2" );

		subItem.addItem( "subsub1" ).setSelected( true );
		subItem.addItem( "subsub2" );

		MenuItem selected = menu.getSelectedItem();
		assertSame( item, selected );

		selected = menu.getLowestSelectedItem();
		assertSame( menu.getItemWithPath( "subsub1" ), selected );
		assertNull( menu.getItemWithPath( "path1" ).getSelectedItem() );

		subItem.setSelected( true );

		assertSame( item, menu.getSelectedItem() );
		assertSame( subItem, menu.getLowestSelectedItem() );
		assertNull( subItem.getSelectedItem() );
	}
}
