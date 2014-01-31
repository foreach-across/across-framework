package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.web.events.BuildMenuEvent;

public class DebugMenuEvent extends BuildMenuEvent<DebugMenu>
{
	public DebugMenuEvent( DebugMenu menu ) {
		super( menu );
	}
}
