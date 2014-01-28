package com.foreach.across.modules.web.menu;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
	private String path, title, url;
	private LinkedList<MenuItem> items = new LinkedList<MenuItem>();

	private Comparator<Menu> comparator = null;
	private boolean comparatorInheritable = false;

	public Menu( String path ) {
		this.path = path;
		this.title = path;
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

	/**
	 * Fetches the first item with the path specified.
	 *
	 * @param path Path of the item.
	 * @return MenuItem instance or null if not found.
	 */
	public MenuItem getItem( String path ) {
		for ( MenuItem item : items ) {
			if ( StringUtils.equals( path, item.getPath() ) ) {
				return item;
			}
		}
		return null;
	}

	public List<MenuItem> getItems() {
		return items;
	}

	public boolean hasItems() {
		return !items.isEmpty();
	}

	public void addItem( String path ) {
		addItem( path, path );
	}

	public void addItem( String path, String title ) {
		MenuItem item = new MenuItem( path );
		item.setTitle( title );

		addItem( item );
	}

	public void addItem( MenuItem item ) {
		items.add( item );
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

			for ( MenuItem item : items ){
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
