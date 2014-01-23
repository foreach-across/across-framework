package com.foreach.across.testweb.other;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.mvc.DebugMenu;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.menu.BuildMenuEvent;
import net.engio.mbassy.listener.Handler;
import org.springframework.web.bind.annotation.RequestMapping;

@AcrossEventHandler
@DebugWebController
public class CustomDebugController
{
	@Handler
	public void buildMenu( BuildMenuEvent<DebugMenu> event ) {
		event.addMenuItem( "/custom", "Custom debug page" );
	}

	@RequestMapping("/custom")
	public DebugPageView hoi( DebugPageView view ) {
		System.out.println( "oi" );

		return view;
	}

}
