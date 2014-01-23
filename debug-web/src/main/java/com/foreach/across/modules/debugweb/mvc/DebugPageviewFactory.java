package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.menu.MenuRenderer;
import org.springframework.beans.factory.annotation.Autowired;

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

	private String debugPageTemplate = "th/layouts/debugPage";

	public String getDebugPageTemplate() {
		return debugPageTemplate;
	}

	public void setDebugPageTemplate( String debugPageTemplate ) {
		this.debugPageTemplate = debugPageTemplate;
	}

	public DebugPageView buildView() {
		DebugPageView view = new DebugPageView( debugPageTemplate );
		view.addObject( "debugMenu", menuFactory.buildMenu( new DebugMenu( debugWebModule.getRootPath() ) ) );

		return view;
	}
}
