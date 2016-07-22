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

import com.foreach.across.core.AcrossException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import java.util.*;

/**
 * <p>Represents a hierarchical menu (tree) structure.</p>
 * <p>All items in the menu can be sorted using a Comparator, the same Comparator can be applied
 * to all submenus in the tree.  By default no comparator is attached and items will be sorted
 * according to the natural order of their title.</p>
 * <p>Alternatively a Menu can be set as Ordered, in which case sorting will have no effect.  The Ordered
 * property must be set on all submenus separately, as it is not inherited.  This means it is possible
 * to provide an inheritable Comparator on the menu, but sort the menu itself manually and only use
 * the Comparator for all submenus.</p>
 * <p><strong>Note that sorting needs to be done explicitly, see {@link #sort()} method.</strong></p>
 */
public class Menu implements Ordered
{
	public static final int ROOT_LEVEL = 0;

	public static final Comparator<Menu> SORT_BY_TITLE = new Comparator<Menu>()
	{
		public int compare( Menu o1, Menu o2 ) {
			String mine = StringUtils.defaultString( o1.getTitle() );
			return mine.compareTo( StringUtils.defaultString( o2.getTitle() ) );
		}
	};

	public static final Comparator<Menu> SORT_BY_ORDER_AND_TITLE = new OrderComparatorWrapper( SORT_BY_TITLE );

	private int order = Ordered.LOWEST_PRECEDENCE - 1000;

	private boolean ordered, selected, group, disabled;
	private String name, path, title, url;

	@SuppressWarnings("all")
	private LinkedList<Menu> items = new LinkedList<>();

	private List<Menu> readonlyItems = Collections.unmodifiableList( items );

	private Comparator<Menu> comparator = null;
	private boolean comparatorInheritable = false;

	private Menu parent;

	private Map<String, Object> attributes = new HashMap<>();

	public Menu() {
		this.path = "";
	}

	public Menu( String name ) {
		this();
		this.name = name;

		setPath( name );
	}

	public Menu( String path, String title ) {
		this( path );
		setTitle( title );
	}

	/**
	 * Copy constructor.
	 *
	 * @param original
	 */
	public Menu( Menu original ) {
		merge( original, false );
	}

	void setParent( Menu parent ) {
		this.parent = parent;
	}

	/**
	 * @return The direct parent of this menu item or null if it is the root of the tree.
	 */
	public Menu getParent() {
		return parent;
	}

	/**
	 * @return The root of the menu tree this item belongs to.
	 */
	public Menu getRoot() {
		return parent != null ? parent.getRoot() : this;
	}

	/**
	 * @return True if this menu item has a parent menu item, false if it is the root.
	 */
	public boolean hasParent() {
		return parent != null;
	}

	/**
	 * @return True if this menu item is the root of the tree.
	 */
	public boolean isRoot() {
		return !hasParent();
	}

	/**
	 * @return The level this item is currently at, level 0 (Menu.ROOT_LEVEL) means it is the root of the menu tree.
	 */
	public int getLevel() {
		int level = 0;

		Menu item = this;
		while ( item.hasParent() ) {
			item = item.getParent();
			level++;
		}

		return level;
	}

	/**
	 * @return True if this Menu is in fact a group or items, but should not be treated as a single item in itself.
	 */
	public boolean isGroup() {
		return group;
	}

	public void setGroup( boolean group ) {
		this.group = group;
	}

	/**
	 * @return True if this Menu should be treated as disabled.
	 */
	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled( boolean disabled ) {
		this.disabled = disabled;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public boolean isOrdered() {
		return ordered;
	}

	/**
	 * Determines if the menu is already ordered.  If true, calls to sort() will have no effect on the menu.
	 * The ordered flag is not inheritable but must be set on every Menu explicitly.
	 *
	 * @param ordered True if the menu is already ordered.
	 */
	public void setOrdered( boolean ordered ) {
		this.ordered = ordered;
	}

	/**
	 * Set the comparator to be used when sorting this Menu.  If the comparator is inheritable, it
	 * will also be used for all submenus in this tree that do not have another comparator set explicitly.
	 *
	 * @param comparator  Comparator instance.
	 * @param inheritable True if the comparator should also be used for all submenus.
	 */
	public void setComparator( Comparator<Menu> comparator, boolean inheritable ) {
		this.comparator = comparator;
		comparatorInheritable = inheritable;
	}

	/**
	 * @return The comparator instance or null if none attached.
	 */
	public Comparator<Menu> getComparator() {
		return comparator;
	}

	/**
	 * @return True if the comparator will be inherited by submenus.  False if no comparator specified
	 * or it cannot be inherited.
	 */
	public boolean isComparatorInheritable() {
		return comparatorInheritable;
	}

	public String getPath() {
		return path;
	}

	public void setPath( String path ) {
		this.path = path;
	}

	public String getUrl() {
		return hasUrl() ? url : path;
	}

	public void setUrl( String url ) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public boolean hasUrl() {
		return url != null;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public boolean hasTitle() {
		return !StringUtils.isBlank( title );
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes( Map<String, Object> attributes ) {
		this.attributes = attributes;
	}

	public void setAttribute( String name, Object value ) {
		attributes.put( name, value );
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute( String name ) {
		return (T) attributes.get( name );
	}

	public boolean hasAttribute( String name ) {
		return attributes.containsKey( name );
	}

	/**
	 * @return True if this Menu is selected (can be the lowest selected item or not).
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @return The explicit order assigned to the menu item.
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Assign an explicit order to the menu item.  Depending on the comparator this property will be used.
	 * By default the menu is sorted first on order value and second on name.
	 *
	 * @param order Explicit order to assign.
	 */
	public void setOrder( int order ) {
		this.order = order;
	}

	/**
	 * Set the selected status of the menu item (and its parents).  Selecting a child will automatically
	 * select its parents.  Deselecting it will not deselect its parents, but deselecting a parent will
	 * deselect the children.
	 *
	 * @param selected True if the menu item and its parents should be selected.
	 */
	public void setSelected( boolean selected ) {
		if ( selected && hasParent() ) {
			getRoot().setSelected( false );
			getParent().setSelected( true );
		}
		else if ( !selected && isSelected() && hasItems() ) {
			for ( Menu item : items ) {
				item.setSelected( false );
			}
		}

		this.selected = selected;
	}

	/**
	 * Returns the selected direct child of this menu.  Will return null if {@link #isSelected()} returns false.
	 *
	 * @return Menu or null if none selected.
	 */
	public Menu getSelectedItem() {
		for ( Menu item : items ) {
			if ( item.isSelected() ) {
				return item;
			}
		}

		return null;
	}

	/**
	 * Returns all selected items (including the menu itself) in top-down order.
	 *
	 * @return Menu items or empty collection if none selected.
	 */
	@SuppressWarnings("all")
	public List<Menu> getSelectedItemPath() {
		LinkedList<Menu> selectedItems = new LinkedList<>();

		Menu item = getLowestSelectedItem();

		// Move up but not past ourselves
		while ( item != null && item != this ) {
			selectedItems.addFirst( item );
			item = item.getParent();
		}

		if ( isSelected() ) {
			selectedItems.addFirst( this );
		}

		return selectedItems;
	}

	/**
	 * Returns the lowest selected item of this menu tree.  Will return null if {@link #isSelected()} returns false.
	 *
	 * @return Menu or null if none selected.
	 */
	public Menu getLowestSelectedItem() {
		Menu item = getSelectedItem();

		if ( item != null && item.hasItems() ) {
			Menu child = item.getLowestSelectedItem();

			if ( child != null ) {
				item = child;
			}
		}

		return item;
	}

	/**
	 * Fetches the item that matches the given MenuSelector.
	 *
	 * @param selector MenuSelector the item should match.
	 * @return Menu instance or null if not found.
	 */
	public Menu getItem( MenuSelector selector ) {
		if ( selector != null ) {
			return selector.find( this );
		}

		return null;
	}

	/**
	 * Sets the item matching the MenuSelector as as selected.
	 *
	 * @param selector MenuSelector the item should match.
	 * @return True if an item was selected.
	 */
	public boolean select( MenuSelector selector ) {
		Menu item = getItem( selector );

		if ( item != null ) {
			item.setSelected( true );
		}

		return item != null;
	}

	/**
	 * Fetches the first item with the path specified.
	 *
	 * @param path Path of the item.
	 * @return Menu instance or null if not found.
	 */
	public Menu getItemWithPath( String path ) {
		return getItem( Menu.byPath( path ) );
	}

	/**
	 * Fetches the first item with the name specified.
	 *
	 * @param name Name of the item.
	 * @return Menu instance or null if not found.
	 */
	public Menu getItemWithName( String name ) {
		return getItem( Menu.byName( name ) );
	}

	public List<Menu> getItems() {
		return readonlyItems;
	}

	public Menu getFirstItem() {
		if ( hasItems() ) {
			return items.getFirst();
		}

		return null;
	}

	public boolean hasItems() {
		return !items.isEmpty();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public Menu addItem( String path ) {
		return addItem( path, path );
	}

	public Menu addItem( String path, String title ) {
		Menu item = new Menu( path );
		item.setTitle( title );

		return addItem( item );
	}

	public Menu addItem( String path, String title, String url ) {
		Menu item = new Menu( path );
		item.setTitle( title );
		item.setUrl( url );

		return addItem( item );
	}

	public Menu addItem( Menu item ) {
		if ( item.hasParent() ) {
			throw new AcrossException( "A Menu can only belong to a single parent menu." );
		}

		items.add( item );
		item.setParent( this );

		if ( item.isSelected() ) {
			item.setSelected( true );
		}

		return item;
	}

	/**
	 * Removes the menu item from the tree - disconnects it from its parent.
	 *
	 * @param item Menu to remove.
	 * @return True if found anywhere in the tree and removed successfully.
	 */
	public boolean remove( Menu item ) {
		if ( !item.hasParent() || item.getRoot() == getRoot() ) {
			if ( item.getParent() == this ) {
				boolean removed = items.remove( item );
				if ( removed ) {
					item.setParent( null );
				}
				return removed;
			}
			else {
				return item.getParent().remove( item );
			}
		}

		return false;
	}

	/**
	 * Sorts the items in the menu recursively depending on Comparator specified or Ordered property.
	 */
	public void sort() {
		sort( null );
	}

	protected void sort( Comparator<Menu> inheritedComparator ) {
		if ( hasItems() ) {
			Comparator<Menu> comparatorToInherit = comparator;
			Comparator<Menu> comparatorToUse = comparator;
			boolean inheritable = comparatorInheritable;

			if ( comparatorToUse == null ) {
				comparatorToUse = inheritedComparator;
				comparatorToInherit = inheritedComparator;
				inheritable = true;
			}
			else if ( !inheritable ) {
				comparatorToInherit = inheritedComparator;
				inheritable = true;
			}

			if ( comparatorToUse == null ) {
				comparatorToUse = SORT_BY_ORDER_AND_TITLE;
				inheritable = false;
			}

			if ( !isOrdered() ) {
				Collections.sort( items, comparatorToUse );
			}

			for ( Menu item : items ) {
				item.sort( inheritable ? comparatorToInherit : null );
			}
		}
	}

	public int size() {
		return items.size();
	}

	public void clear() {
		for ( Menu item : items ) {
			item.setParent( null );
		}
		items.clear();
	}

	/**
	 * <p>
	 * Merges the other menu into this one.
	 * <ul>
	 * <li>Any item with the same path will be modified</li>
	 * <li>Any unknown item present in other will be added</li>
	 * <li>Any item not present in other will be kept</li>
	 * </ul>
	 * In case an item is modified:
	 * <ul>
	 * <li>Properties are overwritten with the values from other</li>
	 * <li>Attributes from other are added or overwritten (merge of attribute map)</li>
	 * <li>All sub items undergo a merge</li>
	 * </ul>
	 * A merge only looks downstream, the parent structure does not find modified.  The selected item
	 * however can be modified by the merge.
	 * </p>
	 *
	 * @param other      Other menu to merge into the current instance.
	 * @param ignoreRoot True if the root of the other Menu should be ignored, only children should be merged.
	 */
	public void merge( Menu other, boolean ignoreRoot ) {
		if ( other != null ) {
			if ( !ignoreRoot ) {
				order = other.order;
				ordered = other.ordered;
				group = other.group;
				disabled = other.disabled;
				name = other.name;
				path = other.path;
				title = other.title;
				url = other.url;
				comparator = other.comparator;
				comparatorInheritable = other.comparatorInheritable;

				setSelected( other.selected );

				for ( Map.Entry<String, Object> otherAttribute : other.getAttributes().entrySet() ) {
					setAttribute( otherAttribute.getKey(), otherAttribute.getValue() );
				}
			}

			// Merge items by path
			for ( Menu otherItem : other.getItems() ) {
				Menu existingItem = findDirectChildWithPath( otherItem.getPath() );

				if ( existingItem != null ) {
					existingItem.merge( otherItem, false );
				}
				else {
					Menu duplicate = new Menu( otherItem );
					duplicate.setParent( null );

					addItem( duplicate );
				}
			}
		}
	}

	private Menu findDirectChildWithPath( String path ) {
		for ( Menu currentItem : items ) {
			if ( StringUtils.equals( currentItem.path, path ) ) {
				return currentItem;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return "Menu{" +
				"name='" + path + '\'' +
				", items=" + items +
				'}';
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given path.
	 *
	 * @param path Path to look for.
	 * @return MenuSelector instance.
	 */
	public static MenuSelector byPath( final String path ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( path, item.getPath() );
			}
		};
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given url.
	 *
	 * @param url URL to look for.
	 * @return MenuSelector instance.
	 */
	public static MenuSelector byUrl( final String url ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( url, item.getUrl() );
			}
		};
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given name.
	 *
	 * @param name Name to look for.
	 * @return MenuSelector instance.
	 */
	public static MenuSelector byName( final String name ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( name, item.getName() );
			}
		};
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given title.
	 *
	 * @param title Title to look for.
	 * @return MenuSelector instance.
	 */
	public static MenuSelector byTitle( final String title ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( title, item.getTitle() );
			}
		};
	}
}
