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
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.menu.MenuSelector;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Event fired by the MenuFactory whenever a menu is being generated.  After menu generation, a menu will be sorted and the selector applied.
 * Customizing the menu is usually done through the {@link PathBasedMenuBuilder}, updating a {@link Menu} after generation can be
 * done by adding a post-processor. Post-processors will receive the generated, sorted menu with active items selected using the configured {@link MenuSelector}.
 *
 * @param <T> Specific Menu implementation
 * @see com.foreach.across.modules.web.menu.MenuFactory
 * @see com.foreach.across.modules.web.menu.MenuBuilder
 * @see PathBasedMenuBuilder
 */
@SuppressWarnings("WeakerAccess")
public class BuildMenuEvent<T extends Menu> implements NamedAcrossEvent, ResolvableTypeProvider
{
	private final T menu;
	private final PathBasedMenuBuilder menuBuilder;
	private final ResolvableType menuResolvableType;

	/**
	 * -- GETTER --
	 * The modifiable list of consumers that should be applied to the generated {@link Menu}
	 * before the {@link MenuFactory} hands it out.
	 */
	@Getter
	private final Collection<Consumer<T>> menuPostProcessors = new ArrayList<>();

	private MenuSelector menuSelector;

	public BuildMenuEvent( T menu ) {
		this( menu, new PathBasedMenuBuilder() );
	}

	public BuildMenuEvent( T menu, PathBasedMenuBuilder menuBuilder ) {
		this( menu, menuBuilder, ResolvableType.forClass( ClassUtils.getUserClass( menu.getClass() ) ) );
	}

	public BuildMenuEvent( @NonNull T menu, @NonNull PathBasedMenuBuilder menuBuilder, @NonNull ResolvableType menuResolvableType ) {
		this.menu = menu;
		this.menuBuilder = menuBuilder;
		this.menuResolvableType = menuResolvableType;
	}

	@Override
	public ResolvableType getResolvableType() {
		ResolvableType classResolvableType = ResolvableType.forClass( getClass() );
		return classResolvableType.hasGenerics()
				? ResolvableType.forClassWithGenerics( getClass(), menuResolvableType )
				: classResolvableType;
	}

	/**
	 * Name for this event, same as the menu name.
	 *
	 * @return event name
	 */
	public String getEventName() {
		return getMenuName();
	}

	/**
	 * Name of the menu being built.
	 *
	 * @return menu name
	 */
	public String getMenuName() {
		return menu.getName();
	}

	/**
	 * Retrieve the menu selector that is attached to this event. A menu selector is optional, but when
	 * one is present it is expected to be used to select the {@link Menu} item that is active.
	 * This is especially useful in a web request scenario where a single menu item represents the location
	 * of the current user web request.
	 * <p/>
	 * You can manually set the menu selector that the {@link com.foreach.across.modules.web.menu.MenuFactory}
	 * should use with {@link #setMenuSelector(MenuSelector)}.
	 *
	 * @return the MenuSelector attached to this event
	 * @see com.foreach.across.modules.web.menu.RequestMenuSelector
	 */
	public MenuSelector getMenuSelector() {
		return menuSelector;
	}

	/**
	 * Set the {@code MenuSelector} that should be used for selecting the active items,
	 * after the {@link Menu} has been built by the {@link com.foreach.across.modules.web.menu.MenuFactory}.
	 * But before any of the post processors are called.
	 *
	 * @param menuSelector that will be used
	 */
	public void setMenuSelector( MenuSelector menuSelector ) {
		this.menuSelector = menuSelector;
	}

	/**
	 * Check if the current build event is for a menu of the given type.
	 *
	 * @param menuClass expected type of menu
	 * @return true if menu is of that type
	 */
	public boolean isForMenuOfType( @NonNull Class<? extends Menu> menuClass ) {
		return menuClass.isInstance( menu );
	}

	/**
	 * Retrieve the item builder for a specific path. If there is none yet, one will be created.
	 * <p/>
	 * Short-hand for {@code builder().item(path)}.
	 *
	 * @param path identifying the item
	 * @return item builder
	 */
	public final PathBasedMenuBuilder.PathBasedMenuItemBuilder item( String path ) {
		return builder().item( path );
	}

	/**
	 * Retrieve the item builder for a specific path, where the item should represent a group of items.
	 * If there is no item builder yet, one will be created. If the item builder exists, but is not yet flagged as
	 * a group, it will be turned into a group.
	 * <p/>
	 * Note that flagging an item as a group simply sets the appropriate property. It has no effect on the actual
	 * {@link Menu} hierarchy being built and the fact that this item might serve as a parent for others.
	 * The latter is purely determined by the path splitting when building the menu.
	 * <p/>
	 * Short-hand for {@code builder().group(path)}.
	 *
	 * @param path identifying the item
	 * @return item builder
	 */
	public final PathBasedMenuBuilder.PathBasedMenuItemBuilder group( String path ) {
		return builder().group( path );
	}

	/**
	 * Return an item builder for updating an item if it exists. This will always return a valid
	 * item builder, but nothing will happen if that item did not exist before.
	 * <p/>
	 * Short-hand for {@code builder().optionalItem(path)}.
	 *
	 * @param path identifying the item
	 * @return item builder
	 */
	public final PathBasedMenuBuilder.PathBasedMenuItemBuilder optionalItem( String path ) {
		return builder().optionalItem( path );
	}

	/**
	 * Get the {@link PathBasedMenuBuilder} being used for configuration of the {@link Menu}.
	 *
	 * @return menu builder
	 * @see #item(String)
	 * @see #optionalItem(String)
	 */
	public PathBasedMenuBuilder builder() {
		return menuBuilder;
	}

	/**
	 * Add a single {@link Consumer} for post-processing the generated {@link Menu}.
	 * This consumer will be called once the menu has been generated, sorted and the selector applied;
	 * but before the {@link com.foreach.across.modules.web.menu.MenuFactory} returns to the caller.
	 *
	 * @param postProcessor for the menu
	 */
	public void addMenuPostProcessor( Consumer<T> postProcessor ) {
		menuPostProcessors.add( postProcessor );
	}

	/**
	 * The menu item that is being built. Usually you do not want to make direct changes to this menu
	 * but use the {@link #builder()} instead. If you want to make modifications after the builder
	 * has been applied, register a custom post-processor using {@link #addMenuPostProcessor(Consumer)}.
	 *
	 * @return menu being built - never {@code null}
	 */
	public T getMenu() {
		return menu;
	}
}
