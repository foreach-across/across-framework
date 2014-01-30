package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.modules.debugweb.mvc.DebugMenu;
import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import com.foreach.across.modules.web.menu.Menu;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@DebugWebController
public class DefaultDebugWebController
{
	@RequestMapping( "" )
	public DebugPageView landingPage( DebugPageView debugPageView ) {
		return debugPageView;
	}
}
