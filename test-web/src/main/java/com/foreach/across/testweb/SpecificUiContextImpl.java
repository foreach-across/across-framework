package com.foreach.across.testweb;

import com.foreach.across.modules.web.ui.AbstractWebUiContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

public class SpecificUiContextImpl extends AbstractWebUiContext<User> implements SpecificUiContext
{
	public SpecificUiContextImpl(
			HttpServletRequest request, HttpServletResponse response ) {
		super( request, response );
	}
}
