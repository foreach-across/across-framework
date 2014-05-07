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
