package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.menu.PrefixContextMenuItemBuilderProcessor;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class DebugMenuBuilder extends RequestMenuBuilder<DebugMenu, DebugMenuEvent>
{
	@Autowired
	private DebugWeb debugWeb;

	@Override
	public DebugMenu build() {
		DebugMenu debugMenu = new DebugMenu();
		setContext( debugMenu );

		return debugMenu;
	}

	@Override
	protected DebugMenuEvent createEvent( DebugMenu menu ) {
		PathBasedMenuBuilder menuBuilder = new PathBasedMenuBuilder( new PrefixContextMenuItemBuilderProcessor(
				debugWeb ) );

		menuBuilder.root( "/" ).title( "DebugWebModule" );

		return new DebugMenuEvent( menu, menuBuilder );
	}
}
