package com.foreach.across.modules.debugweb;

import com.foreach.across.modules.web.context.PrefixingPathContext;

public final class DebugWeb extends PrefixingPathContext
{
	public static final String MODULE = "DebugWebModule";
	public static final String VIEWS = "debugweb";

	public static final String CSS_MAIN = "/css/debugweb/debugweb.css";

	public static final String LAYOUT_TEMPLATE = "th/debugweb/layouts/debugPage";

	public static final String VIEW_LOGGERS = "th/debugweb/listLoggers";
	public static final String VIEW_SPRING_BEANS = "th/debugweb/listBeans";
	public static final String VIEW_SPRING_INTERCEPTORS = "th/debugweb/listInterceptors";
	public static final String VIEW_PROPERTIES = "th/debugweb/listProperties";
	public static final String VIEW_APPLICATION_PROPERTIES = "th/debugweb/listApplicationProperties";
	public static final String VIEW_THREADS = "th/debugweb/listThreads";
	public static final String VIEW_MODULES = "th/debugweb/listAcrossModules";

	public DebugWeb( String prefix ) {
		super( prefix );
	}
}
