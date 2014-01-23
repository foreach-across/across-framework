package com.foreach.across.modules.debugweb.mvc;

import org.springframework.web.servlet.ModelAndView;

/**
 * Represents a standard DebugWebController result page.
 * Defines the basic template for the debug page and holds the menu.
 */
public class DebugPageView extends ModelAndView
{
	DebugPageView( String parentView ) {
		super( parentView );
	}

	public void setPageTemplate( String pageTemplate ) {
		setViewName( pageTemplate );
	}

	public void setPage( String pageName ) {
		addObject( "subPage", pageName );
	}
}
