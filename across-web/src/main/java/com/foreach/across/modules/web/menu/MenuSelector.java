package com.foreach.across.modules.web.menu;

/**
 * A MenuSelector is a strategy interface that searches a Menu tree and returns at most one Menu item that matches it.
 */
public interface MenuSelector
{
	/**
	 * Search the given Menu for an item that matches the current selector.
	 *
	 * @param menu Menu tree to search top-down.
	 * @return Matching Menu item or null if not found.
	 */
	Menu find( Menu menu );
}
