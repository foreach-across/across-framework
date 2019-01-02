/*
 * Copyright 2019 the original author or authors
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

/**
 * A PathBasedMenuBuilder can be used to define menu items in a non-hierarchical way.
 * Items are registered using a unique path. Every item with a path that is also the prefix
 * of another item's path, will become the parent item of those other items.
 * <p/>
 * This will result in a sub-tree being created.
 * The <strong>/</strong> (forward slash) is the path separator for prefix candidates.
 * <h4>Example</h4>
 * <p>
 * Suppose you register the following menu items in order:
 * <ol>
 * <li>/my-group/item-1</li>
 * <li>/my-group</li>
 * <li>/my-item</li>
 * <li>/my-group/item-2</li>
 * <li>/my-other-group/single-item</li>
 * </ol>
 * </p>
 * <p>
 * or in code:
 * <pre>{@code
 * builder.item( "/my-group/item-1" ).and()
 *        .item( "/my-group" ).and()
 *        .item( "/my-item" ).and()
 *        .item( "/my-group/item-2" ).and()
 *        .item( "/my-other-group/single-item" ).and()
 *        .item( "/my-group:item-3" ).and()
 *        .build()
 * }</pre>
 * When building the {@link Menu} this will result in the following hierarchy:
 * <pre>{@code
 * ROOT
 *   + /my-group
 *   |   + /my-group/item-1
 *   |   + /my-group/item-2
 *   + /my-group:item-3
 *   + /my-item
 *   + /my-other-group/single-item
 * }</pre>
 * </p>
 * Note that a parent item does not automatically get created based on path separator (the example of {@code /my-other-group/single-item}.
 * Only if there is another item with an exact path of that before a separator will an item be created. This is also why {@code /my-group:item-3}
 * is still a separate item as <strong>:</strong> (colon) does not count as a path separator.
 * <p/>
 * By default the top-most item of the menu has no specific path. Setting a path on the root item
 * can be done using {@link #root(String)}, but this will have no impact on the hierarchy being created.
 * The root path of a {@code Menu} is only relevant in specialized cases where you want to merge the result of a
 * builder into an already existing {@code Menu}.
 * <h4>Order and sorting</h4>
 * <p>A {@code Menu} item can have an order specified which can be used to sort the {@code Menu}. Sorting a menu is usually done externally,
 * the {@link PathBasedMenuBuilder} creates a {@code Menu} with the items ordered according to their path value. You can see this in the above example.</p>
 * <p>You can manually set a {@code Comparator} that should be used for sorting a menu. You can so do per item, in which case the comparator
 * will be used only for the children of that particular item. Depending on how you register it, it will apply for all sub-trees of a menu item.</p>
 * <p>If you want to use a different {@code Comparator} for the entire menu, you must register it on the root item:
 * <pre>{@code
 * builder.root( "/root" ).comparator( myComparator, true );
 * }</pre>
 * </p>
 * <h4>Group items</h4>
 * <p>
 * A single item can also be flagged as a group (this sets the value of {@link Menu#isGroup()}. This property however has nothing to do with
 * the actual hierarchy being created. It is not because an item is flagged as a group that it will be turned into a parent for other items.
 * It will automatically serve as a parent if the other items have a path that uses the current item as prefix. The group property is an indication
 * for how the item should behave in the generated {@link Menu}. The value of {@link Menu#isGroup()} is never set automatically, not even when
 * an item becomes the parent of others. You should create groups using {@link #group(String)}.
 * </p>
 * <h4>From item path to Menu hierarchy</h4>
 * <p>The {@code PathBaseMenuBuilder} has itself no concept of a hierarchy of menu items. It purely works on a map of items identified by their path.
 * It is only when the actual {@link Menu} is being built that this flat list of items gets turned into a {@link Menu} hierarchy, based on the
 * presence of path separators (<strong>/</strong> - forward slash) and items matching sub-segments of others.
 *
 * @author Arne Vandamme, Marc Vanbrabant
 * @see MenuFactory
 * @see Menu
 * @see MenuSelector
 * @see RequestMenuSelector
 * @see RequestMenuBuilder
 * @since 1.0.0
 */
public class PathBasedMenuBuilder
{
	@Getter(AccessLevel.PROTECTED)
	private final PathBasedMenuBuilder parent;

	private final PathBasedMenuItemBuilder rootBuilder;
	private final Map<String, PathBasedMenuItemBuilder> itemBuilders;

	/**
	 * List of consumers that will be executes the first time {@link #build()} is being called.
	 * These allow modifying the actual builder itself, but after initial modifications have been done.
	 * <p/>
	 * Mainly useful if you want to perform moves of groups and you want to make sure that all items have been added.
	 */
	private final Deque<Consumer<PathBasedMenuBuilder>> beforeBuildConsumers;

	private final MenuItemBuilderProcessor itemProcessor;

	public PathBasedMenuBuilder() {
		this( null, MenuItemBuilderProcessor.NoOpProcessor );
	}

	public PathBasedMenuBuilder( MenuItemBuilderProcessor itemProcessor ) {
		this( null, itemProcessor );
	}

	protected PathBasedMenuBuilder( PathBasedMenuBuilder parent, MenuItemBuilderProcessor itemProcessor ) {
		this.parent = parent;
		this.itemProcessor = itemProcessor;

		if ( parent != null ) {
			rootBuilder = parent.rootBuilder;
			itemBuilders = parent.itemBuilders;
			beforeBuildConsumers = parent.beforeBuildConsumers;
		}
		else {
			rootBuilder = new PathBasedMenuItemBuilder( null, this, false );
			itemBuilders = new ConcurrentSkipListMap<>();
			beforeBuildConsumers = new ArrayDeque<>();
		}
	}

	/**
	 * Perform a set of actions with a different item processor.
	 * This creates a new builder based on this one but having a different processor applied after menu item building.
	 * The new builder will have the same configuration, and any item builders modified will immediately apply
	 * to both the original and the new builder. Only when building will a different processor for these items be used.
	 * <p/>
	 * Use this method if you want to apply custom processing to a subset of menu items in this menu.
	 * This does not change the current builder, only creates a new one that is passed to the consumer.
	 * <p/>
	 * If you want to effectively remove an already attached processor, you can use this method
	 * with the {@link MenuItemBuilderProcessor#NoOpProcessor} as parameter.
	 *
	 * @param processor that should be applied to the scoped version
	 * @param consumer  for performing actions on the scoped builder
	 * @return original menu builder
	 * @see MenuItemBuilderProcessor#NoOpProcessor
	 */
	public PathBasedMenuBuilder withProcessor( @NonNull MenuItemBuilderProcessor processor, @NonNull Consumer<PathBasedMenuBuilder> consumer ) {
		consumer.accept( new PathBasedMenuBuilder( this, processor ) );
		return this;
	}

	/**
	 * Get the root item builder. By default the root item of the menu has no path,
	 * this method also configures a path on the root item.
	 *
	 * @param rootPath path of the top most menu
	 * @return root item builder
	 */
	public PathBasedMenuItemBuilder root( @NonNull String rootPath ) {
		rootBuilder.path = rootPath;
		return rootBuilder;
	}

	/**
	 * Retrieve the item builder for a specific path. If there is none yet, one will be created.
	 *
	 * @param path identifying the item
	 * @return item builder
	 */
	public PathBasedMenuItemBuilder item( @NonNull String path ) {
		PathBasedMenuItemBuilder itemBuilder = itemBuilders.get( path );

		if ( itemBuilder == null ) {
			itemBuilder = new PathBasedMenuItemBuilder( path, this, false );
			itemBuilders.put( itemBuilder.path, itemBuilder );
		}

		return itemBuilder;
	}

	/**
	 * Return an item builder for updating an item if it exists. This will always return a valid
	 * item builder, but nothing will happen if that item did not exist before.
	 *
	 * @param path identifying the item
	 * @return item builder
	 */
	public PathBasedMenuItemBuilder optionalItem( @NonNull String path ) {
		PathBasedMenuItemBuilder itemBuilder = itemBuilders.get( path );

		if ( itemBuilder == null ) {
			return new PathBasedMenuItemBuilder( path, this, true );
		}

		return itemBuilder;
	}

	/**
	 * Retrieve the item builder for a specific path. If there is none yet, one will be created.
	 * <p/>
	 * This method is a shorter version for {@code item(path).title(title)}.
	 *
	 * @param path  identifying the item
	 * @param title that should be set on the item
	 * @return item builder
	 */
	public PathBasedMenuItemBuilder item( String path, String title ) {
		return item( path ).title( title );
	}

	/**
	 * Retrieve the item builder for a specific path. If there is none yet, one will be created.
	 * <p/>
	 * This method is a shorter version for {@code item(path).title(title).url(url)}.
	 *
	 * @param path  identifying the item
	 * @param title that should be set on the item
	 * @param url   that should be set on the item
	 * @return item builder
	 */
	public PathBasedMenuItemBuilder item( String path, String title, String url ) {
		return item( path ).title( title ).url( url );
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
	 * This method is a shorter version for {@code item(path).group(true)}.
	 *
	 * @param path identifying the item
	 * @return item builder
	 */
	public PathBasedMenuItemBuilder group( String path ) {
		return item( path ).group( true );
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
	 * This method is a shorter version for {@code item(path, title).group(true)}.
	 *
	 * @param path  identifying the item
	 * @param title that should be set on the item
	 * @return item builder
	 */
	public PathBasedMenuItemBuilder group( String path, String title ) {
		return item( path, title ).group( true );
	}

	/**
	 * Add an additional {@link Consumer} for this {@link PathBasedMenuBuilder} that should be applied right before
	 * the actual {@link Menu} is being built.
	 * <p/>
	 * Use this method sparingly, only when you want to customize the menu builder and wish to be sure that initial
	 * configuration has been applied. For example you want to move a group of items but you are not sure up front how
	 * many child items it will have as modules could register them at a later stage.
	 * Shifting the path changing calls to the separate {@code andThen(Consumer)} consumer can help you in said case.
	 * <p/>
	 * Note that once the consumer has been applied, it will not be re-applied on subsequent builds.
	 * It is possible to register additional consumers from within a consumer, but simplicity's sake you probably want to avoid this.
	 *
	 * @param consumer to add
	 * @return current builder
	 */
	public PathBasedMenuBuilder andThen( @NonNull Consumer<PathBasedMenuBuilder> consumer ) {
		beforeBuildConsumers.addLast( consumer );
		return this;
	}

	/**
	 * Remove the item with the specified path from this menu builder. Optionally also removes all other items
	 * having the specified path as prefix.
	 *
	 * @param path                           to remove
	 * @param removeAllItemsWithPathAsPrefix true if other items with that path as prefix should be removed as well
	 * @return current builder
	 */
	public PathBasedMenuBuilder removeItems( @NonNull String path, boolean removeAllItemsWithPathAsPrefix ) {
		updateOrRemoveItems( path, null, removeAllItemsWithPathAsPrefix );
		return this;
	}

	/**
	 * Update the path of all items starting with (or equal to) the given path prefix.
	 * The prefix of the current path will be updated to the new path prefix.
	 * If you only want to change the single item with exactly that path,
	 * use {@code changeItemPath("currentPrefix", "newPrefix", false)}.
	 * <p/>
	 * Note that, unlike {@code item("my item").changePathTo(X)}, this method will not create
	 * any items if they do not exist.
	 *
	 * @param currentPathPrefix prefix item paths should be starting with
	 * @param newPathPrefix     new prefix to use
	 * @return current menu builder
	 * @see #changeItemPath(String, String, boolean)
	 * @since 3.0.0
	 */
	public PathBasedMenuBuilder changeItemPath( @NonNull String currentPathPrefix, @NonNull String newPathPrefix ) {
		return changeItemPath( currentPathPrefix, newPathPrefix, true );
	}

	/**
	 * Update the path of all items starting with (or equal to) the given path prefix.
	 * The prefix of the current path will be updated to the new path prefix.
	 * If you only want to change the single item starting with exactly that path,
	 * use {@code changeItemPaths("currentPrefix", "newPrefix", false)}.
	 * <p/>
	 * Note that, unlike {@code item("my item").changePathTo(X)}, this method will not create
	 * any items if they do not exist.
	 *
	 * @param currentPathPrefix         prefix item paths should be starting with
	 * @param newPathPrefix             new prefix to use
	 * @param replaceAllItemsWithPrefix true if all items starting with the currentPathPrefix should be updated, false if only an exact match should update
	 * @return current menu builder
	 * @since 3.0.0
	 */
	public PathBasedMenuBuilder changeItemPath( @NonNull String currentPathPrefix, @NonNull String newPathPrefix, boolean replaceAllItemsWithPrefix ) {
		updateOrRemoveItems( currentPathPrefix, newPathPrefix, replaceAllItemsWithPrefix );
		return this;
	}

	private void updateOrRemoveItems( String currentPathPrefix, String newPathPrefix, boolean replaceAllItemsWithPrefix ) {
		Collection<String> keys = new ArrayList<>( itemBuilders.keySet() );
		keys.forEach( key -> {
			PathBasedMenuItemBuilder menuItemBuilder = itemBuilders.get( key );
			String suffixedPath = suffixPath( currentPathPrefix );
			if ( StringUtils.equals( key, currentPathPrefix ) || ( replaceAllItemsWithPrefix && StringUtils.startsWith( key, suffixedPath ) ) ) {
				String updatePath = newPathPrefix != null ? StringUtils.replaceOnce( menuItemBuilder.path, currentPathPrefix, newPathPrefix ) : null;
				updateItemBuilder( key, menuItemBuilder, updatePath );
			}
		} );
	}

	private void updateItemBuilder( String existingKey, PathBasedMenuItemBuilder menuItemBuilder, String newPath ) {
		itemBuilders.remove( existingKey );
		if ( newPath != null ) {
			itemBuilders.put( newPath, menuItemBuilder.path( newPath ) );
		}
	}

	/**
	 * @return A newly constructed Menu instance.
	 */
	public Menu build() {
		applyBeforeBuildConsumers();

		Menu root = rootBuilder.build();
		Menu current = root;

		Map<Menu, String> pathMap = new HashMap<>();

		for ( Map.Entry<String, PathBasedMenuItemBuilder> builderEntry : itemBuilders.entrySet() ) {
			String path = builderEntry.getKey();
			PathBasedMenuItemBuilder itemBuilder = builderEntry.getValue();
			Menu item = itemBuilder.build();

			pathMap.put( item, path );

			while ( !current.isRoot() && !path.startsWith( suffixPath( pathMap.get( current ) ) ) ) {
				current = current.getParent();
			}

			current.addItem( item );
			current = item;
		}

		return root;
	}

	private void applyBeforeBuildConsumers() {
		while ( !beforeBuildConsumers.isEmpty() ) {
			beforeBuildConsumers.removeFirst().accept( this );
		}
	}

	private String suffixPath( String path ) {
		return path.endsWith( "/" ) ? path : path + "/";
	}

	/**
	 * Will build into the existing menu and merge the root only if the root has been configured
	 * on the builder.
	 *
	 * @param menu Menu instance that should contain the result.
	 * @see #build(Menu, boolean)
	 */
	public void build( Menu menu ) {
		build( menu, rootBuilder.path == null );
	}

	/**
	 * Will merge into the existing menu and merge the root only if it has been configured on the builder.
	 *
	 * @param menu Menu instance that should contain the result.
	 * @see #merge(Menu, boolean)
	 */
	public void merge( Menu menu ) {
		merge( menu, rootBuilder.path == null );
	}

	/**
	 * Builds the result of the builder in the existing instance.  This clears the instance and considers it as the root
	 * for the new menu.  Only the root node will be merged and the reference to the parent will be kept,
	 * all existing child items will be deleted.  If you want to merge the 2 menu items, see Merge instead.
	 *
	 * @param menu       Menu instance that should contain the result.
	 * @param ignoreRoot True if the root of the original menu should be kept as is.
	 * @see #merge(Menu, boolean)
	 */
	public void build( Menu menu, boolean ignoreRoot ) {
		menu.clear();
		merge( menu, ignoreRoot );
	}

	/**
	 * Merges the result of the builder in the existing instance.  Any items already present in the existing menu
	 * will be kept or modified if they have the same path.  If you do not care about the existing instance, use
	 * build instead.
	 *
	 * @param menu       Menu instance that should contain the result.
	 * @param ignoreRoot True if the root of the original menu should be kept as is.
	 * @see #build(Menu, boolean)
	 */
	public void merge( Menu menu, boolean ignoreRoot ) {
		Menu newMenu = build();
		newMenu.setName( menu.getName() );
		menu.merge( newMenu, ignoreRoot );
	}

	/**
	 * Represents a single item builder attached to a parent {@link PathBasedMenuBuilder}.
	 */
	@Accessors(fluent = true)
	public final static class PathBasedMenuItemBuilder implements Comparable<PathBasedMenuItemBuilder>
	{
		private final PathBasedMenuBuilder menuBuilder;
		private final Map<String, Object> attributes = new HashMap<>();
		private final boolean optional;

		private boolean disabled;
		private Comparator<Menu> comparator;
		private boolean inheritableComparator;
		private Integer order;

		@Setter(AccessLevel.PRIVATE)
		private String path;

		/**
		 * -- SETTER --
		 * Set the 'group' flag on the menu item.
		 */
		@Setter
		private boolean group;

		/**
		 * -- SETTER --
		 * Set the title for this menu item.
		 */
		@Setter
		private String title;

		/**
		 * -- SETTER --
		 * Set the url for this menu item.
		 */
		@Setter
		private String url;

		private PathBasedMenuItemBuilder( String path, PathBasedMenuBuilder menuBuilder, boolean optional ) {
			this.path = path;
			this.menuBuilder = menuBuilder;
			this.optional = optional;
		}

		/**
		 * Set the comparator to be used when sorting the children of this menu item.
		 * If the comparator is inheritable, it will also be used for the sub-menus in the resulting tree,
		 * unless they have another comparator set explicitly.
		 * <p/>
		 * Note that depending on the custom comparator you set, the value of {@link #order(int)} might be ignored.
		 *
		 * @param comparator  to use
		 * @param inheritable should the comparator also apply to any resulting sub-menus of this item
		 * @return current builder
		 */
		public PathBasedMenuItemBuilder comparator( Comparator<Menu> comparator, boolean inheritable ) {
			this.comparator = comparator;
			this.inheritableComparator = inheritable;
			return this;
		}

		/**
		 * Set the order of this menu item.
		 *
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder order( int order ) {
			this.order = order;
			return this;
		}

		/**
		 * Flag this menu item as disabled.
		 *
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder disable() {
			this.disabled = true;
			return this;
		}

		/**
		 * Flag this menu item as enabled.
		 *
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder enable() {
			this.disabled = false;
			return this;
		}

		/**
		 * Flag this menu item as disabled or enabled.
		 *
		 * @param status disabled or not
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder disable( boolean status ) {
			this.disabled = status;
			return this;
		}

		/**
		 * Flag this menu item as disabled or enabled.
		 *
		 * @param status enabled or not
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder enable( boolean status ) {
			this.disabled = !status;
			return this;
		}

		/**
		 * Add a single attribute to this menu item.
		 *
		 * @param key   attribute key
		 * @param value attribute value
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder attribute( String key, Object value ) {
			attributes.put( key, value );
			return this;
		}

		/**
		 * Add one or more option attributes to this menu item.
		 * An option attribute is an attribute where the key is identical to the value.
		 * For every argument X, an attribute with key X and value X will be added.
		 *
		 * @param options to add
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder options( String... options ) {
			for ( String option : options ) {
				attributes.put( option, option );
			}
			return this;
		}

		/**
		 * Remove the attributes with the given keys.
		 *
		 * @param keys of the attributes to remove
		 * @return current item builder
		 */
		public PathBasedMenuItemBuilder removeAttributes( String... keys ) {
			for ( String key : keys ) {
				attributes.remove( key );
			}
			return this;
		}

		/**
		 * Add prefix strings on which this menu item will match in case a {@link RequestMenuSelector} is being used.
		 * If the request starts with any of these prefixes, this menu item will be selected.
		 * <p>
		 * These values only apply if the selector supports it. This is usually the case for menus being built
		 * through the {@link MenuFactory} using a {@link RequestMenuBuilder} behind the scened.</p>
		 *
		 * @param matchers one or more prefix strings
		 * @see com.foreach.across.modules.web.menu.RequestMenuSelector
		 */
		public PathBasedMenuItemBuilder matchRequests( String... matchers ) {
			attributes.put( RequestMenuSelector.ATTRIBUTE_MATCHERS, Arrays.asList( matchers ) );
			return this;
		}

		/**
		 * Remove this item. Optionally removing all other items with this path as prefix.
		 * This method returns the menu builder that this item belonged to, destroying the item builder.
		 *
		 * @param removeAllItemsWithPrefix true if all other items having this prefix should also be removed
		 * @return menu builder
		 */
		public PathBasedMenuBuilder remove( boolean removeAllItemsWithPrefix ) {
			if ( !optional ) {
				menuBuilder.updateOrRemoveItems( path, null, removeAllItemsWithPrefix );
			}
			return and();
		}

		/**
		 * Change the path of this menu item, as well as all other items that have the current path as a prefix.
		 * This path change will happen immediately and after this call a new item can be created on the previous path.
		 * <p/>
		 * If you only want to change the path of the current item but not all other items starting with this path,
		 * use {@code changePathTo("new path", false)}.
		 *
		 * @param newPath new path that should be used
		 * @return current item builder
		 * @see #changePathTo(String, boolean)
		 */
		public PathBasedMenuItemBuilder changePathTo( @NonNull String newPath ) {
			return changePathTo( newPath, true );
		}

		/**
		 * Change the path of this menu item, optionally changing other items that have the current path as a prefix.
		 * This path change will happen immediately and after this call a new item can be created on the previous path.
		 *
		 * @param newPath                   new path that should be used
		 * @param replaceAllItemsWithPrefix false if only the current item should have its path changed
		 * @return current item builder
		 * @see #changePathTo(String, boolean)
		 */
		public PathBasedMenuItemBuilder changePathTo( @NonNull String newPath, boolean replaceAllItemsWithPrefix ) {
			if ( !optional ) {
				menuBuilder.updateOrRemoveItems( path, newPath, replaceAllItemsWithPrefix );
			}
			return this;
		}

		/**
		 * Go up a level, switch from the item builder back to the menu builder.
		 *
		 * @return menu builder this item belongs to
		 */
		public PathBasedMenuBuilder and() {
			return menuBuilder;
		}

		private Menu build() {
			Menu menu = new Menu( path == null ? "" : path, title );
			menu.setUrl( url );

			if ( order != null ) {
				menu.setOrder( order );
			}

			if ( comparator != null ) {
				menu.setComparator( comparator, inheritableComparator );
			}

			menu.setGroup( group );
			menu.setAttributes( attributes );
			menu.setDisabled( disabled );

			return menuBuilder.itemProcessor.process( menu );
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			PathBasedMenuItemBuilder that = (PathBasedMenuItemBuilder) o;
			return path != null ? path.equals( that.path ) : that.path == null;
		}

		@Override
		public int hashCode() {
			return path != null ? path.hashCode() : 0;
		}

		@Override
		public int compareTo( PathBasedMenuItemBuilder o ) {
			return path.compareTo( o.path );
		}

		@Override
		public String toString() {
			return "PathBasedMenuItemBuilder{" +
					"path='" + path + '\'' +
					'}';
		}
	}
}
