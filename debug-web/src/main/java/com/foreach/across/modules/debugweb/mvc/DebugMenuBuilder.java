package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.web.menu.BuildMenuEvent;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class DebugMenuBuilder extends RequestMenuBuilder
{
	@Autowired
	private DebugWebModule debugWebModule;

	@Override
	public <T extends Menu> T build( Class<T> menuType ) {
		DebugMenu debugMenu = new DebugMenu( debugWebModule.getRootPath());
		setContext( debugMenu );

		return (T) debugMenu;
	}

	@Override
	public <T extends Menu> BuildMenuEvent<T> buildEvent( T menu ) {
		return (BuildMenuEvent<T>) new DebugMenuEvent( (DebugMenu) menu );
	}
}
