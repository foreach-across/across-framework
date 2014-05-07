package com.foreach.across.modules.web.events;

import com.foreach.across.core.events.NamedAcrossEvent;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuSelector;
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

	public BuildMenuEvent( T menu ) {
		this.menu = menu;
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
