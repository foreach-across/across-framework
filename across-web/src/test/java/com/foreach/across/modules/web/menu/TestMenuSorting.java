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

import org.junit.Test;
import org.springframework.core.Ordered;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

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

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		Menu otherSubMenu = new Menu( "/ccc", "bbb" );
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
	public void multiLevelSortingWithOrderValues() {
		Menu menu = new Menu( "any" );

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.addItem( "111", "333" ).setOrder( Ordered.HIGHEST_PRECEDENCE );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" ).setOrder( Ordered.LOWEST_PRECEDENCE );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		Menu otherSubMenu = new Menu( "/ccc", "bbb" );
		otherSubMenu.addItem( "111", "111" );
		otherSubMenu.addItem( "222", "333" );
		otherSubMenu.addItem( "333", "222" ).setOrder( 1 );
		menu.addItem( otherSubMenu );

		menu.sort();

		assertMenu( menu, "aaa", "bbb", "ccc" );
		assertFalse( menu.getItems().get( 0 ).hasItems() );
		assertTrue( menu.getItems().get( 1 ).hasItems() );
		assertMenu( menu.getItems().get( 1 ), "222", "111", "333" );
		assertTrue( menu.getItems().get( 2 ).hasItems() );
		assertMenu( menu.getItems().get( 2 ), "333", "222", "111" );
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

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.setOrdered( true );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		Menu otherSubMenu = new Menu( "/ccc", "bbb" );
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

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "111" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "aaa" );

		Menu otherSubMenu = new Menu( "/ccc", "bbb" );
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

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.setComparator( Menu.SORT_BY_ORDER_AND_TITLE, false );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		menu.addItem( subMenu );

		Menu subSubMenu = new Menu( "333", "111" );
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

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.setComparator( Menu.SORT_BY_ORDER_AND_TITLE, true );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		menu.addItem( subMenu );

		Menu subSubMenu = new Menu( "333", "111" );
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

		Menu subMenu = new Menu( "/aaa", "ccc" );
		subMenu.setComparator( Menu.SORT_BY_ORDER_AND_TITLE, true );
		subMenu.setOrdered( true );
		subMenu.addItem( "111", "333" );
		subMenu.addItem( "222", "222" );
		menu.addItem( subMenu );

		Menu subSubMenu = new Menu( "333", "111" );
		subSubMenu.addItem( "111", "111" );
		subSubMenu.addItem( "222", "333" );
		subSubMenu.addItem( "333", "222" );
		subMenu.addItem( subSubMenu );

		menu.addItem( "/bbb", "aaa" );
		menu.addItem( "/ccc", "bbb" );

		menu.sort();

		assertMenu( menu, "ccc", "bbb", "aaa" );
		assertMenu( menu.getItem( Menu.byPath( "/aaa" ) ), "333", "222", "111" );
		assertMenu( menu.getItem( Menu.byPath( "/aaa" ) ).getItem( Menu.byPath( "333" ) ), "111", "222", "333" );
	}

	@Test
	public void fixedOrderComparator() {
		Menu menu = new Menu( "any" );
		Menu subMenu = new Menu( "/aaa", "aaa" );
		subMenu.addItem( "111", "111" );
		subMenu.addItem( "222", "222" );
		subMenu.addItem( "333", "333" );
		menu.addItem( subMenu );

		menu.addItem( "/bbb", "bbb" );

		Menu other = new Menu( "/ccc", "ccc" );
		other.addItem( "ddd", "fff" );
		other.addItem( "eee", "eee" );
		other.addItem( "fff", "ddd" );
		menu.addItem( other );

		menu.setComparator(
				new FixedMenuOrderComparator( MenuMatchers.pathEquals( "/bbb" ), MenuMatchers.pathMatches( "^/c" ),
				                              MenuMatchers.pathMatches( ".*a.*" ), MenuMatchers.pathEquals( "333" ),
				                              MenuMatchers.pathMatches( "2" ),
				                              MenuMatchers.pathMatches( Pattern.compile( "111" ) ) ), true
		);

		menu.sort();

		assertMenu( menu, "bbb", "ccc", "aaa" );
		assertMenu( menu.getItem( Menu.byPath( "/aaa" ) ), "333", "222", "111" );
		assertMenu( menu.getItem( Menu.byPath( "/ccc" ) ), "ddd", "eee", "fff" );
	}

	public void assertMenu( Menu menu, String... expected ) {
		List<Menu> items = menu.getItems();
		assertEquals( expected.length, items.size() );
		for ( int i = 0; i < items.size(); i++ ) {
			assertEquals( expected[i], items.get( i ).getTitle() );
		}
	}
}
