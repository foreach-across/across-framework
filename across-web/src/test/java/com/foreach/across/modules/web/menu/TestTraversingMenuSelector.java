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
