package com.foreach.across.modules.web.menu;

/**
 * Processor instance to modify menu items after they have been built,
 * but before having been attached to a parent item.
 */
public interface MenuItemBuilderProcessor
{
	MenuItemBuilderProcessor NoOpProcessor = new MenuItemBuilderProcessor()
	{
		@Override
		public Menu process( Menu menu ) {
			return menu;
		}
	};

	Menu process( Menu menu );
}
