package com.foreach.across.modules.web.menu;

/**
 * Simple implementation of MenuSelector that will iterate over all items
 * in a menu tree and find the lowest item that matches.
 */
public abstract class TraversingMenuSelector implements MenuSelector
{
	private boolean findLowest;

	/**
	 * If the boolean parameter is true, the entire tree will be scanned for the lowest matching item.
	 * If false, the first matching item will be returned.
	 *
	 * @param findLowest True if the lowest matching item should be found.
	 */
	protected TraversingMenuSelector( boolean findLowest ) {
		this.findLowest = findLowest;
	}

	public boolean isFindLowest() {
		return findLowest;
	}

	public void setFindLowest( boolean findLowest ) {
		this.findLowest = findLowest;
	}

	public Menu find( Menu menu ) {
		Menu found = null;

		for ( Menu item : menu.getItems() ) {
			if ( findLowest ) {
				if ( matches( item ) && isLower( item, found ) ) {
					found = item;
				}

				if ( item.hasItems() ) {
					Menu lower = item.getItem( this );

					if ( lower != null && isLower( lower, found ) ) {
						found = lower;
					}
				}
			}
			else {
				if ( matches( item ) ) {
					found = item;
				}
				else if ( item.hasItems() ) {
					found = item.getItem( this );
				}

				if ( found != null ) {
					return found;
				}
			}
		}

		return found;
	}

	private boolean isLower( Menu candidate, Menu current ) {
		return current == null || candidate.getLevel() >= current.getLevel();
	}

	protected abstract boolean matches( Menu item );
}
