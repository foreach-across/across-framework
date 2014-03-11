package com.foreach.across.test.modules.web.menu;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuItem;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class TestMenuSorting
{
	private static final Comparator<Menu> REVERSE_SORT = new Comparator<Menu>()
	{
		public int compare( Menu o1, Menu o2 ) {
			return o2.getTitle().compareTo( o1.getTitle() );
		}
	};

	@Test
	public void singleLevelSorting() {
		Menu menu = new Menu( "any" );
		menu.addItem( "/aaa", "ccc" );
		menu.addItem( "/bbb", "aaa" );
		menu.addItem( "/ccc", "bbb" );

		menu.sort();

		assertMenu( menu, "aaa", "bbb", "ccc" );
	}

	@Test
	public void multiLevelSorting() {
		Menu menu = new Menu( "any" );

		MenuItem subMenu = new MenuItem( "/aaa", "ccc" );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		MenuItem otherSubMenu = new MenuItem( "/ccc", "bbb" );
		otherSubMenu.addItem( "111", "111" );
		otherSubMenu.addItem( "222", "333" );
		otherSubMenu.addItem( "333", "222" );
		menu.addItem( otherSubMenu );

		menu.sort();

		assertMenu( menu, "aaa", "bbb", "ccc" );
		assertFalse( menu.getItems().get( 0 ).hasItems() );
		assertTrue( menu.getItems().get( 1 ).hasItems() );
		assertMenu( menu.getItems().get( 1 ), "111", "222", "333" );
		assertTrue( menu.getItems().get( 2 ).hasItems() );
		assertMenu( menu.getItems().get( 2 ), "111", "222", "333" );
	}

	@Test
	public void orderedMenuIsNotSorted() {
		Menu menu = new Menu( "any" );
		menu.setOrdered( true );
		menu.addItem( "/aaa", "ccc" );
		menu.addItem( "/bbb", "aaa" );
		menu.addItem( "/ccc", "bbb" );

		menu.sort();

		assertMenu( menu, "ccc", "aaa", "bbb" );
	}

	@Test
	public void orderedMustBeSetOnAllMenusExplicitly() {
		Menu menu = new Menu( "any" );
		menu.setOrdered( true );

		MenuItem subMenu = new MenuItem( "/aaa", "ccc" );
		subMenu.setOrdered( true );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		MenuItem otherSubMenu = new MenuItem( "/ccc", "bbb" );
		otherSubMenu.addItem( "111", "111" );
		otherSubMenu.addItem( "222", "333" );
		otherSubMenu.addItem( "333", "222" );
		menu.addItem( otherSubMenu );

		menu.sort();

		assertMenu( menu, "ccc", "aaa", "bbb" );
		assertMenu( menu.getItems().get( 0 ), "333", "222", "111" );
		assertMenu( menu.getItems().get( 2 ), "111", "222", "333" );
	}

	@Test
	public void comparatorCanBeInherited() {
		Menu menu = new Menu( "any" );
		menu.setComparator( REVERSE_SORT, true );

		MenuItem subMenu = new MenuItem( "/aaa", "ccc" );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		MenuItem otherSubMenu = new MenuItem( "/ccc", "bbb" );
		otherSubMenu.addItem( "111", "111" );
		otherSubMenu.addItem( "222", "333" );
		otherSubMenu.addItem( "333", "222" );
		menu.addItem( otherSubMenu );

		menu.sort();

		assertMenu( menu, "ccc", "bbb", "aaa" );
		assertMenu( menu.getItemWithPath( "/aaa" ), "333", "222", "111" );
		assertMenu( menu.getItemWithPath( "/ccc" ), "333", "222", "111" );
	}

	@Test
	public void comparatorCanBeNonInheritable() {
		Menu menu = new Menu( "any" );
		menu.setComparator( REVERSE_SORT, true );

		MenuItem subMenu = new MenuItem( "/aaa", "ccc" );
		subMenu.setComparator( Menu.SORT_BY_TITLE, false );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		menu.addItem( subMenu );

		MenuItem subSubMenu = new MenuItem( "333", "111" );
		subSubMenu.addItem( "111", "111" );
		subSubMenu.addItem( "222", "333" );
		subSubMenu.addItem( "333", "222" );
		subMenu.addItem( subSubMenu );

		menu.addItem( "/bbb", "aaa" );
		menu.addItem( "/ccc", "bbb" );

		menu.sort();

		assertMenu( menu, "ccc", "bbb", "aaa" );
		assertMenu( menu.getItemWithPath( "/aaa" ), "111", "222", "333" );
		assertMenu( menu.getItemWithPath( "/aaa" ).getItemWithPath( "333" ), "333", "222", "111" );
	}

	@Test
	public void comparatorIsInheritedUnlessOverruled() {
		Menu menu = new Menu( "any" );
		menu.setComparator( REVERSE_SORT, true );

		MenuItem subMenu = new MenuItem( "/aaa", "ccc" );
		subMenu.setComparator( Menu.SORT_BY_TITLE, true );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		menu.addItem( subMenu );

		MenuItem subSubMenu = new MenuItem( "333", "111" );
		subSubMenu.addItem( "111", "111" );
		subSubMenu.addItem( "222", "333" );
		subSubMenu.addItem( "333", "222" );
		subMenu.addItem( subSubMenu );

		menu.addItem( "/bbb", "aaa" );
		menu.addItem( "/ccc", "bbb" );

		menu.sort();

		assertMenu( menu, "ccc", "bbb", "aaa" );
		assertMenu( menu.getItemWithPath( "/aaa" ), "111", "222", "333" );
		assertMenu( menu.getItemWithPath( "/aaa" ).getItemWithPath( "333" ), "111", "222", "333" );
	}

	@Test
	public void orderedHasPriorityOverAComparator() {
		Menu menu = new Menu( "any" );
		menu.setComparator( REVERSE_SORT, true );

		MenuItem subMenu = new MenuItem( "/aaa", "ccc" );
		subMenu.setComparator( Menu.SORT_BY_TITLE, true );
		subMenu.setOrdered( true );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		menu.addItem( subMenu );

		MenuItem subSubMenu = new MenuItem( "333", "111" );
		subSubMenu.addItem( "111", "111" );
		subSubMenu.addItem( "222", "333" );
		subSubMenu.addItem( "333", "222" );
		subMenu.addItem( subSubMenu );

		menu.addItem( "/bbb", "aaa" );
		menu.addItem( "/ccc", "bbb" );

		menu.sort();

		assertMenu( menu, "ccc", "bbb", "aaa" );
		assertMenu( menu.getItemWithPath( "/aaa" ), "333", "222", "111" );
		assertMenu( menu.getItemWithPath( "/aaa" ).getItemWithPath( "333" ), "111", "222", "333" );
	}

	public void assertMenu( Menu menu, String... expected ) {
		List<MenuItem> items = menu.getItems();
		assertEquals( expected.length, items.size() );
		for ( int i = 0; i < items.size(); i++ ) {
			assertEquals( expected[i], items.get( i ).getTitle() );
		}
	}
}
