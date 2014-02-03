package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.context.bootstrap.BootstrapAcrossModuleOrder;
import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.mvc.DebugMenuEvent;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@AcrossEventHandler
@DebugWebController
public class AcrossInfoController
{
	@Autowired
	private AcrossContext acrossContext;

	@Handler
	public void buildMenu( DebugMenuEvent event ) {
		event.addMenuItem( "/across/modules", "Across modules" );
	}

	@RequestMapping("/across/modules")
	public DebugPageView showBeans( DebugPageView view ) {
		view.setPage( DebugWeb.VIEW_MODULES );

		BootstrapAcrossModuleOrder modules = new BootstrapAcrossModuleOrder( acrossContext.getModules() );

		view.addObject( "moduleRegistry", modules );
		view.addObject( "modules", modules.getOrderedModules() );

		return view;
	}
}
