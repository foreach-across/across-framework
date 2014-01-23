package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.mvc.DebugMenu;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.menu.BuildMenuEvent;
import com.foreach.across.modules.web.table.Table;
import net.engio.mbassy.listener.Handler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@AcrossEventHandler
@DebugWebController
public class PropertiesController
{
	@Handler
	public void buildMenu( BuildMenuEvent<DebugMenu> event ) {
		event.addMenuItem( "/properties/environment", "System environment variables" );
	}

	@RequestMapping("/properties/environment")
	public DebugPageView listEnvironmentProperties( DebugPageView view ) {
		view.setPage( "th/listProperties" );

		view.addObject( "systemProperties", Table.fromMap( "System properties", System.getProperties() ) );
		view.addObject( "environmentVariables", Table.fromMap( "Environment variables", System.getenv() ) );

		return view;
	}
}
