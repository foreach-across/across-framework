package com.foreach.across.test.modules.web.menu;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.TraversingMenuSelector;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class TestTraversingMenuSelector
{
	@Test
	public void findFirstItem() {
		Menu menu = new Menu();
		Menu item1 = menu.addItem( "path1" );
		Menu item2 = menu.addItem( "path2" );
		Menu subItem1 = item2.addItem( "path1" );
		Menu subItem2 = item2.addItem( "sub2" );
		Menu subSubItem1 = subItem1.addItem( "subsub1" );
		Menu subSubItem2 = subItem2.addItem( "path1" );

		Menu found = menu.getItem( new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( "path1", item.getPath() );
			}
		} );

		assertSame( item1, found );
	}

	@Test
	public void findLowestItem() {
		Menu menu = new Menu();
		Menu item1 = menu.addItem( "path1" );
		Menu item2 = menu.addItem( "path2" );
		Menu subItem1 = item2.addItem( "path1" );
		Menu subItem2 = item2.addItem( "sub2" );
		Menu subSubItem1 = subItem1.addItem( "subsub1" );
		Menu subSubItem2 = subItem2.addItem( "path1" );

		Menu found = menu.getItem( new TraversingMenuSelector( true )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( "path1", item.getPath() );
			}
		} );

		assertSame( subSubItem2, found );
	}
}
