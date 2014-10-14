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

package com.foreach.across.modules.web.events;

import com.foreach.across.core.events.NamedAcrossEvent;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuSelector;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * Event fired by the MenuFactory whenever a menu is being generated.  After menu generation,
 * a menu will be sorted and selected.
 *
 * @param <T> Specific Menu implementation
 * @see com.foreach.across.modules.web.menu.MenuFactory
 * @see com.foreach.across.modules.web.menu.MenuBuilder
 */
public class BuildMenuEvent<T extends Menu> implements NamedAcrossEvent
{
	private T menu;
	private MenuSelector selector;
	private PathBasedMenuBuilder menuBuilder;

	public BuildMenuEvent( T menu ) {
		this.menu = menu;
		this.menuBuilder = new PathBasedMenuBuilder();
	}

	public BuildMenuEvent( T menu, PathBasedMenuBuilder menuBuilder ) {
		this.menu = menu;
		this.menuBuilder = menuBuilder;
	}

	public PathBasedMenuBuilder builder() {
		return menuBuilder;
	}

	public String getEventName() {
		return getMenuName();
	}

	public String getMenuName() {
		return menu.getName();
	}

	/**
	 * @return The MenuSelector attached to this event.
	 */
	public MenuSelector getSelector() {
		return selector;
	}

	public void setSelector( MenuSelector selector ) {
		this.selector = selector;
	}

	public boolean forMenu( Class<? extends Menu> menuClass ) {
		return menuClass.isAssignableFrom( menu.getClass() );
	}

	public boolean forMenu( String menuName ) {
		return StringUtils.equals( menuName, getMenuName() );
	}

	public T getMenu() {
		return menu;
	}

	public Menu getItem( MenuSelector selector ) {
		return menu.getItem( selector );
	}

	public Menu getItemWithName( String name ) {
		return menu.getItemWithName( name );
	}

	public Menu getItemWithPath( String path ) {
		return menu.getItemWithPath( path );
	}

	public Menu addItem( String path, String title ) {
		return menu.addItem( path, title );
	}

	public Menu addItem( Menu item ) {
		return menu.addItem( item );
	}
}
