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
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A PathBasedMenuBuilder can be used to define menu items in a non-hierarchical way.
 * When building the actual Menu, the path will be used to determine parent and sub-menus.
 * Any new prefix will be used as root of a sub menu.
 */
public class PathBasedMenuBuilder
{
	private final PathBasedMenuBuilder parent;

	private final PathBasedMenuItemBuilder rootBuilder;
	private final Map<String, PathBasedMenuItemBuilder> itemBuilders;
	private final Map<String, String> moves;
	private final MenuItemBuilderProcessor itemProcessor;

	public PathBasedMenuBuilder() {
		this( null, MenuItemBuilderProcessor.NoOpProcessor );
	}

	public PathBasedMenuBuilder( MenuItemBuilderProcessor itemProcessor ) {
		this( null, itemProcessor );
	}

	public PathBasedMenuBuilder( PathBasedMenuBuilder parent,
	                             MenuItemBuilderProcessor itemProcessor ) {
		this.parent = parent;
		this.itemProcessor = itemProcessor;

		if ( parent != null ) {
			rootBuilder = parent.rootBuilder;
			itemBuilders = parent.itemBuilders;
			moves = parent.moves;
		}
		else {
			rootBuilder = new PathBasedMenuItemBuilder( null, this );
			itemBuilders = new TreeMap<>();
			moves = new TreeMap<>();
		}
	}

	/**
	 * @return A new builder with the specified processor.
	 */
	public PathBasedMenuBuilder builder( MenuItemBuilderProcessor processor ) {
		Assert.notNull( "A processor must be specified - try using NoOpProcessor if you want to clear it." );
		return new PathBasedMenuBuilder( this, processor );
	}

	/**
	 * @return The parent PathBasedMenuBuilder or the current one if there is no parent.
	 */
	public PathBasedMenuBuilder and() {
		return parent != null ? parent : this;
	}

	public PathBasedMenuItemBuilder root( String rootPath ) {
		Assert.notNull( "Root path must not be null." );
		rootBuilder.path = rootPath;
		return rootBuilder;
	}

	public PathBasedMenuItemBuilder item( String path ) {
		Assert.notNull( path, "A Menu item must have a valid path." );

		PathBasedMenuItemBuilder itemBuilder = itemBuilders.get( path );

		if ( itemBuilder == null ) {
			itemBuilder = new PathBasedMenuItemBuilder( path, this );
			itemBuilders.put( itemBuilder.getPath(), itemBuilder );
		}

		return itemBuilder;
	}

	public PathBasedMenuItemBuilder group( String path ) {
		return item( path ).group( true );
	}

	public PathBasedMenuItemBuilder group( String path, String title ) {
		return item( path, title ).group( true );
	}

	public PathBasedMenuItemBuilder item( String path, String title ) {
		return item( path ).title( title );
	}

	public PathBasedMenuItemBuilder item( String path, String title, String url ) {
		return item( path ).title( title ).url( url );
	}

	/**
	 * @return A newly constructed Menu instance.
	 */
	public Menu build() {
		Menu root = rootBuilder.build();
		Menu current = root;

		Map<String, PathBasedMenuItemBuilder> builderMap = itemBuilders;

		if ( !moves.isEmpty() ) {
			builderMap = new TreeMap<>();

			for ( PathBasedMenuItemBuilder itemBuilder : itemBuilders.values() ) {
				String newPath = determineActualPath( itemBuilder.getPath() );

				builderMap.put( newPath, itemBuilder );
			}
		}

		Map<Menu, String> pathMap = new HashMap<>();

		for ( Map.Entry<String, PathBasedMenuItemBuilder> builderEntry : builderMap.entrySet() ) {
			String path = builderEntry.getKey();
			PathBasedMenuItemBuilder itemBuilder = builderEntry.getValue();
			Menu item = itemBuilder.build();

			pathMap.put( item, path );

			while ( !current.isRoot() && !path.startsWith( pathMap.get( current ) ) ) {
				current = current.getParent();
			}

			current.addItem( item );
			current = item;
		}

		return root;
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
		menu.merge( newMenu, ignoreRoot );
	}

	private String determineActualPath( String path ) {
		String prefix = path;
		String destination = path;

		for ( Map.Entry<String, String> pathEntry : moves.entrySet() ) {
			if ( StringUtils.startsWith( path, pathEntry.getKey() ) ) {
				prefix = pathEntry.getKey();
				destination = pathEntry.getValue();
			}
		}

		return StringUtils.replaceOnce( path, prefix, destination );
	}

	public PathBasedMenuBuilder move( String path, String destinationPath ) {
		Assert.notNull( path, "A valid path must be specified." );
		Assert.notNull( destinationPath, "Cant move to null destination path" );
		moves.put( path, destinationPath );
		return this;
	}

	public PathBasedMenuBuilder undoMove( String path ) {
		moves.remove( path );
		return this;
	}

	public static class PathBasedMenuItemBuilder implements Comparable<PathBasedMenuItemBuilder>
	{
		private final PathBasedMenuBuilder menuBuilder;
		private String path;

		private Integer order;
		private boolean group, disabled;
		private String title, url;

		private Map<String, Object> attributes = new HashMap<>();

		PathBasedMenuItemBuilder( String path, PathBasedMenuBuilder menuBuilder ) {
			this.path = path;
			this.menuBuilder = menuBuilder;
		}

		public String getPath() {
			return path;
		}

		public String getTitle() {
			return title;
		}

		public Integer getOrder() {
			return order;
		}

		public boolean isGroup() {
			return group;
		}

		public boolean isDisabled() {
			return disabled;
		}

		public PathBasedMenuItemBuilder title( String title ) {
			this.title = title;
			return this;
		}

		public String getUrl() {
			return url;
		}

		public PathBasedMenuItemBuilder url( String url ) {
			this.url = url;
			return this;
		}

		public PathBasedMenuItemBuilder group( boolean isGroup ) {
			this.group = isGroup;
			return this;
		}

		public PathBasedMenuItemBuilder order( int order ) {
			this.order = order;
			return this;
		}

		public PathBasedMenuItemBuilder clearOrder() {
			this.order = null;
			return this;
		}

		public PathBasedMenuItemBuilder disable() {
			this.disabled = true;
			return this;
		}

		public PathBasedMenuItemBuilder enable() {
			this.disabled = false;
			return this;
		}

		public PathBasedMenuItemBuilder disable( boolean status ) {
			this.disabled = status;
			return this;
		}

		public PathBasedMenuItemBuilder enable( boolean status ) {
			this.disabled = !status;
			return this;
		}

		public PathBasedMenuItemBuilder attribute( String key, Object value ) {
			attributes.put( key, value );
			return this;
		}

		public PathBasedMenuItemBuilder options( String... options ) {
			for ( String option : options ) {
				attributes.put( option, option );
			}
			return this;
		}

		public PathBasedMenuItemBuilder removeAttributes( String... keys ) {
			for ( String key : keys ) {
				attributes.remove( key );
			}
			return this;
		}

		/**
		 * Add prefix strings on which this menu item will match in case a RequestMenuSelector
		 * is being used.  If the request starts with any of these prefixes, this menu item will be selected.
		 * <p/>
		 * These values only apply if the selector supports it, and currently only RequestMenuSelector does so.
		 *
		 * @param matchers One or more prefix strings.
		 * @see com.foreach.across.modules.web.menu.RequestMenuSelector
		 */
		public void matchRequests( String... matchers ) {
			attributes.put( RequestMenuSelector.ATTRIBUTE_MATCHERS, Arrays.asList( matchers ) );
		}

		public PathBasedMenuBuilder and() {
			return menuBuilder;
		}

		protected Menu build() {
			Menu menu = new Menu( path == null ? "" : path, title );
			menu.setUrl( url );

			if ( order != null ) {
				menu.setOrder( order );
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

			if ( path != null ? !path.equals( that.path ) : that.path != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return path != null ? path.hashCode() : 0;
		}

		@Override
		public int compareTo( PathBasedMenuItemBuilder o ) {
			return getPath().compareTo( o.getPath() );
		}

		@Override
		public String toString() {
			return "PathBasedMenuItemBuilder{" +
					"path='" + path + '\'' +
					'}';
		}
	}
}
