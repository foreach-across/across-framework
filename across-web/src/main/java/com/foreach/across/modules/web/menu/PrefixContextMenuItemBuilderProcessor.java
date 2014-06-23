package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.context.PrefixingPathContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Applies a prefix context to a newly create menu item: applies the prefix to the url
 * and creates an extra matcher for the prefix path.
 */
public class PrefixContextMenuItemBuilderProcessor implements MenuItemBuilderProcessor
{
	private final PrefixingPathContext context;

	public PrefixContextMenuItemBuilderProcessor( PrefixingPathContext context ) {
		this.context = context;
	}

	@Override
	public Menu process( Menu menu ) {
		menu.setUrl( context.path( menu.getUrl() ) );

		Set<String> totalMatchers = new HashSet<>();
		Collection<String> matchers = menu.getAttribute( RequestMenuSelector.ATTRIBUTE_MATCHERS );

		if ( matchers != null ) {
			totalMatchers.addAll( matchers );
		}

		totalMatchers.add( context.path( menu.getPath() ) );

		menu.setAttribute( RequestMenuSelector.ATTRIBUTE_MATCHERS, totalMatchers );

		return menu;
	}
}
