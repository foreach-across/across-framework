package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;

public class DebugMenuEvent extends BuildMenuEvent<DebugMenu>
{
	public DebugMenuEvent( DebugMenu menu, PathBasedMenuBuilder menuBuilder ) {
		super( menu, menuBuilder );
	}
}
