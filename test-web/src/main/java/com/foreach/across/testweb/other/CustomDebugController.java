package com.foreach.across.testweb.other;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import net.engio.mbassy.listener.Handler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@AcrossEventHandler
@DebugWebController
public class CustomDebugController
{
	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addItem( "/custom", "Custom debug page" );
	}

	@RequestMapping("/custom")
	public String hoi( Model model ) {
		System.out.println( "oi" );

		return "";
	}

}
