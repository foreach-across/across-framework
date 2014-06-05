package com.foreach.across.modules.adminweb;

import com.foreach.across.modules.web.context.PrefixingPathContext;

public final class AdminWeb extends PrefixingPathContext
{
	public static final String MODULE = "AdminWebModule";

	public static final String LAYOUT_TEMPLATE_CSS = "/css/adminweb/adminweb.css";
	public static final String LAYOUT_TEMPLATE = "th/adminweb/layouts/adminPage";

	public AdminWeb( String prefix ) {
		super( prefix );
	}
}
