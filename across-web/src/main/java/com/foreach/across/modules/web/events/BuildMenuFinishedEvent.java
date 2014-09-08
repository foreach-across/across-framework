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
import org.apache.commons.lang3.StringUtils;

/**
 * Event fired by the MenuFactory whenever a menu has been build, sorted and the corresponding item has been selected.
 * Can be used to modify the menu after build has completed.
 */
public class BuildMenuFinishedEvent implements NamedAcrossEvent
{
	private Menu menu;
	private MenuSelector selector;

	public BuildMenuFinishedEvent( Menu menu, MenuSelector selector ) {
		this.menu = menu;
		this.selector = selector;
	}

	public String getEventName() {
		return getMenuName();
	}

	public String getMenuName() {
		return menu.getName();
	}

	public MenuSelector getSelector() {
		return selector;
	}

	public boolean forMenu( Class<? extends Menu> menuClass ) {
		return menuClass.isAssignableFrom( menu.getClass() );
	}

	public boolean forMenu( String menuName ) {
		return StringUtils.equals( menuName, getMenuName() );
	}

	public Menu getMenu() {
		return menu;
	}
}
