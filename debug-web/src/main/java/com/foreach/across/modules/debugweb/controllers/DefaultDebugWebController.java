package com.foreach.across.modules.debugweb.controllers;

import com.foreach.across.modules.debugweb.mvc.DebugPageView;
import com.foreach.across.modules.debugweb.mvc.DebugWebController;
import org.springframework.web.bind.annotation.RequestMapping;

@DebugWebController
public class DefaultDebugWebController
{
	@RequestMapping( "" )
	public DebugPageView landingPage( DebugPageView debugPageView ) {
		return debugPageView;
	}
}
