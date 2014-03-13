package com.foreach.across.modules.web.menu;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.web.events.BuildMenuEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MenuFactory
{
	private static final Logger LOG = LoggerFactory.getLogger( MenuFactory.class );

	@Autowired
	private AcrossEventPublisher publisher;

	private Map<Class, MenuStore> menuStoreMap = new HashMap<Class, MenuStore>();
	private Map<Class, MenuBuilder> menuBuilderMap = new HashMap<Class, MenuBuilder>();

	private MenuStore defaultMenuStore;
	private MenuBuilder defaultMenuBuilder;

	private Set<MenuStore> menuStores = new HashSet<MenuStore>();

	public MenuStore getDefaultMenuStore() {
		return defaultMenuStore;
	}

	public void setDefaultMenuStore( MenuStore defaultMenuStore ) {
		this.defaultMenuStore = defaultMenuStore;
		menuStores.add( defaultMenuStore );
	}

	public MenuBuilder getDefaultMenuBuilder() {
		return defaultMenuBuilder;
	}

	public void setDefaultMenuBuilder( MenuBuilder defaultMenuBuilder ) {
		this.defaultMenuBuilder = defaultMenuBuilder;
	}

	public void addMenuStore( MenuStore store, Class<? extends Menu>... menuTypes ) {
		for ( Class menuType : menuTypes ) {
			menuStoreMap.put( menuType, store );
		}
		menuStores.add( store );
	}

	public void addMenuBuilder( MenuBuilder builder, Class<? extends Menu>... menuTypes ) {
		for ( Class menuType : menuTypes ) {
			LOG.info( "Registering menu builder for type {}: {}", menuType, builder );
			menuBuilderMap.put( menuType, builder );
		}
	}

	/**
	 * Fetches a generic menu with the given name.  If any store already has a menu with the given name,
	 * the existing menu will be returned.
	 *
	 * @param name Name of the menu.
	 * @return Menu instance.
	 */
	public Menu buildMenu( String name ) {
		Menu menu = getMenuWithName( name );

		if ( menu == null ) {
			menu = build( name, Menu.class );

			MenuStore menuStore = getMenuStore( menu.getClass() );
			menuStore.save( name, menu );
		}

		return menu;
	}

	public <T extends Menu> T buildMenu( String name, Class<T> menuType ) {
		MenuStore menuStore = getMenuStore( menuType );
		T menu = menuStore.get( name, menuType );

		if ( menu == null ) {
			menu = build( name, menuType );

			menuStore.save( name, menu );
		}

		return menu;
	}

	/**
	 * Performs the build action on the given menu: publishes the build event, sorts
	 * and executes the selector attached to the event.
	 *
	 * @param menu Menu instance being built.
	 * @param <T>  Actual Menu implementation.
	 * @return Menu instance after build has completed.
	 */
	public <T extends Menu> T buildMenu( T menu ) {
		MenuBuilder builder = getMenuBuilder( menu.getClass() );

		BuildMenuEvent<T> buildMenuEvent = builder.buildEvent( menu );
		publisher.publish( buildMenuEvent );

		menu.sort();
		menu.select( buildMenuEvent.getSelector() );

		return menu;
	}

	private MenuBuilder getMenuBuilder( Class<? extends Menu> menuType ) {
		if ( menuBuilderMap.containsKey( menuType ) ) {
			return menuBuilderMap.get( menuType );
		}

		return defaultMenuBuilder;
	}

	private MenuStore getMenuStore( Class<? extends Menu> menuType ) {
		if ( menuStoreMap.containsKey( menuType ) ) {
			return menuStoreMap.get( menuType );
		}

		return defaultMenuStore;
	}

	private <T extends Menu> T build( String name, Class<T> menuType ) {
		MenuBuilder builder = getMenuBuilder( menuType );

		T menu = builder.build( menuType );
		menu.setName( name );

		BuildMenuEvent<T> buildMenuEvent = builder.buildEvent( menu );
		publisher.publish( buildMenuEvent );

		// Always sort a menu after the initial build
		menu.sort();
		menu.select( buildMenuEvent.getSelector() );

		return menu;
	}

	/**
	 * Will search all MenuStore instances for a menu with the given name.
	 *
	 * @param name Name of the menu.
	 * @return Menu or null
	 */
	public Menu getMenuWithName( String name ) {
		Menu existing = defaultMenuStore.get( name );

		if ( existing != null ) {
			return existing;
		}

		for ( MenuStore store : menuStoreMap.values() ) {
			existing = store.get( name );

			if ( existing != null ) {
				return existing;
			}
		}

		return null;
	}
}
