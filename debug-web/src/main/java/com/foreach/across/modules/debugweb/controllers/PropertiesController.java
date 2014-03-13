package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.table.Table;
import net.engio.mbassy.listener.Handler;
import org.springframework.web.bind.annotation.RequestMapping;

@DebugWebController
public class PropertiesController
{
	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addItem( "/properties/environment", "System environment variables" );
	}

	@RequestMapping("/properties/environment")
	public DebugPageView listEnvironmentProperties( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_PROPERTIES );

		view.addObject( "systemProperties", Table.fromMap( "System properties", System.getProperties() ) );
		view.addObject( "environmentVariables", Table.fromMap( "Environment variables", System.getenv() ) );

		return view;
	}
}
