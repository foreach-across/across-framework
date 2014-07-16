package com.foreach.across.modules.web;

public interface AcrossWebModuleSettings
{
	/**
	 * Prefix for JSP view resolver path.
	 */
	public static final String JSP_VIEW_PREFIX = "acrossweb.jsp.prefix";

	/**
	 * Suffix for JSP view resolver path.
	 */
	public static final String JSP_VIEW_SUFFIX = "acrossweb.jsp.suffix";

	/**
	 * True if a default WebTemplateRegistry should be created with support
	 * for WebTemplateProcessors.
	 */
	public static final String TEMPLATES_ENABLED = "acrossweb.templates.enabled";

	/**
	 * True if NamedWebTemplateProcessor instances should automatically register themselves
	 * in the registry. Only relevant if templates are enabled.
	 */
	public static final String TEMPLATES_AUTO_REGISTER = "acrossweb.templates.autoregister";
}
