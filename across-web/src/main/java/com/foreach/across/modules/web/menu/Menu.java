package com.foreach.across.modules.web.menu;

import org.apache.commons.lang3.StringUtils;

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
 * <p/>
 * <strong>Note that sorting needs to be done explicitly, see {@link #sort()} method.</strong>
 */
public class Menu
{
	public static final Comparator<Menu> SORT_BY_TITLE = new Comparator<Menu>()
	{
		public int compare( Menu o1, Menu o2 ) {
			String mine = StringUtils.defaultString( o1.getTitle() );
			return mine.compareTo( StringUtils.defaultString( o2.getTitle() ) );
		}
	};

	private boolean ordered = false;
	private boolean selected = false;
	private String name, path, title, url;
	private LinkedList<MenuItem> items = new LinkedList<MenuItem>();
	private List<MenuItem> readonlyItems = Collections.unmodifiableList( items );

	private Comparator<Menu> comparator = null;
	private boolean comparatorInheritable = false;

	private Menu parent;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	public Menu() {
		this.path = "";
	}

	public Menu( String name ) {
		this();
		this.name = name;
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
	 * @return True if this MenuItem is selected (can be the lowest selected item or not).
	 */
	public boolean isSelected() {
		return selected;
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
			for ( MenuItem item : items ) {
				item.setSelected( false );
			}
		}

		this.selected = selected;
	}

	/**
	 * Returns the selected direct child of this menu.  Will return null if {@link #isSelected()} returns false.
	 *
	 * @return MenuItem or null if none selected.
	 */
	public MenuItem getSelectedItem() {
		for ( MenuItem item : items ) {
			if ( item.isSelected() ) {
				return item;
			}
		}

		return null;
	}

	/**
	 * Returns the lowest selected item of this menu tree.  Will return null if {@link #isSelected()} returns false.
	 *
	 * @return MenuItem or null if none selected.
	 */
	public MenuItem getLowestSelectedItem() {
		MenuItem item = getSelectedItem();

		if ( item != null && item.hasItems() ) {
			MenuItem child = item.getLowestSelectedItem();

			if ( child != null ) {
				item = child;
			}
		}

		return item;
	}

	/**
	 * Fetches the first item with the path specified.
	 *
	 * @param path Path of the item.
	 * @return MenuItem instance or null if not found.
	 */
	public MenuItem getItemWithPath( String path ) {
		MenuItem found = null;

		for ( MenuItem item : items ) {
			if ( StringUtils.equals( path, item.getPath() ) ) {
				found = item;
			}
			else if ( item.hasItems() ) {
				found = item.getItemWithPath( path );
			}

			if ( found != null ) {
				return found;
			}
		}

		return null;
	}

	public MenuItem getItemWithTitle( String title ) {
		for ( MenuItem item : items ) {
			if ( StringUtils.equals( title, item.getTitle() ) ) {
				return item;
			}
		}
		return null;
	}

	public MenuItem getItemWithName( String name ) {
		for ( MenuItem item : items ) {
			if ( StringUtils.equals( name, item.getName() ) ) {
				return item;
			}
		}
		return null;
	}

	public List<MenuItem> getItems() {
		return readonlyItems;
	}

	public boolean hasItems() {
		return !items.isEmpty();
	}

	public MenuItem addItem( String path ) {
		return addItem( path, path );
	}

	public MenuItem addItem( String path, String title ) {
		MenuItem item = new MenuItem( path );
		item.setTitle( title );

		return addItem( item );
	}

	public MenuItem addItem( MenuItem item ) {
		if ( item.hasParent() ) {
			throw new RuntimeException( "A MenuItem can only belong to a single parent menu." );
		}

		items.add( item );
		item.setParent( this );

		return item;
	}

	/**
	 * Removes the menu item from the tree - disconnects it from its parent.
	 *
	 * @param item MenuItem to remove.
	 * @return True if found anywhere in the tree and removed successfully.
	 */
	public boolean remove( MenuItem item ) {
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
				comparatorToUse = SORT_BY_TITLE;
				inheritable = false;
			}

			if ( !isOrdered() ) {
				Collections.sort( items, comparatorToUse );
			}

			for ( MenuItem item : items ) {
				item.sort( inheritable ? comparatorToInherit : null );
			}
		}
	}

	@Override
	public String toString() {
		return "Menu{" +
				"name='" + path + '\'' +
				", items=" + items +
				'}';
	}
}
