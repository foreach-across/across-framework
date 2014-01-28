package com.foreach.across.modules.debugweb.mvc;

import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Represents a standard DebugWebController result page.
 * Defines the basic template for the debug page and holds the menu.
 */
public class DebugPageView extends ModelAndView
{
	private final String debugRoot;

	DebugPageView( String parentView, String debugRoot ) {
		super( parentView );

		this.debugRoot = debugRoot;

		addObject( "debugRoot", debugRoot );
	}

	public void setPageTemplate( String pageTemplate ) {
		setViewName( pageTemplate );
	}

	public void setPage( String pageName ) {
		addObject( "subPage", pageName );
	}

	public String redirect( String path ) {
		String redirect = "redirect:" + debugRoot + path;
		setViewName( redirect );

		return redirect;
	}
}
