package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DebugMenuBuilder extends RequestMenuBuilder<DebugMenu, DebugMenuEvent>
{
	// TODO: use DebugWeb bean here
	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private DebugWebModule debugWebModule;

	@Override
	public DebugMenu build() {
		DebugMenu debugMenu = new DebugMenu( debugWebModule.getRootPath() );
		setContext( debugMenu );

		return debugMenu;
	}

	@Override
	protected DebugMenuEvent createEvent( DebugMenu menu ) {
		return new DebugMenuEvent( menu );
	}
}
