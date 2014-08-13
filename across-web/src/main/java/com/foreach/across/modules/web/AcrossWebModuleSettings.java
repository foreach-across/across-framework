package com.foreach.across.modules.web;

public interface AcrossWebModuleSettings
{
	/**
	 * Prefix for JSP view resolver path.
	 */
	String JSP_VIEW_PREFIX = "acrossWeb.jsp.prefix";

	/**
	 * Suffix for JSP view resolver path.
	 */
	String JSP_VIEW_SUFFIX = "acrossWeb.jsp.suffix";

	/**
	 * True if a default WebTemplateRegistry should be created with support
	 * for WebTemplateProcessors.
	 */
	String TEMPLATES_ENABLED = "acrossWeb.templates.enabled";

	/**
	 * True if NamedWebTemplateProcessor instances should automatically register themselves
	 * in the registry. Only relevant if templates are enabled.
	 */
	String TEMPLATES_AUTO_REGISTER = "acrossWeb.templates.autoregister";
}
