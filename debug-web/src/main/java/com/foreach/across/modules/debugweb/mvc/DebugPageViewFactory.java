package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.debugweb.DebugWeb;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.web.menu.MenuFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Service responsible for creating a DebugView instance.
 * Usually called through a DebugViewArgumentResolver.
 */
public class DebugPageViewFactory
{
	@Autowired
	private MenuFactory menuFactory;

	@Autowired
	private DebugWebModule debugWebModule;

	private String debugPageTemplate = DebugWeb.LAYOUT_MAIN;

	public String getDebugPageTemplate() {
		return debugPageTemplate;
	}

	public void setDebugPageTemplate( String debugPageTemplate ) {
		this.debugPageTemplate = debugPageTemplate;
	}

	public DebugPageView buildView() {
		DebugPageView view = new DebugPageView( debugPageTemplate, debugWebModule.getRootPath() );
		menuFactory.buildMenu( "debugMenu", DebugMenu.class );

//		view.addObject( "debugMenu", menuFactory.buildMenu( new DebugMenu( debugWebModule.getRootPath() ) ) );

		return view;
	}
}
