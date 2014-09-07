package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;

public class AcrossWebModuleSettings extends AcrossModuleSettings
{
	/**
	 * Prefix for JSP view resolver path.
	 */
	public static final String JSP_VIEW_PREFIX = "acrossWeb.jsp.prefix";

	/**
	 * Suffix for JSP view resolver path.
	 */
	public static final String JSP_VIEW_SUFFIX = "acrossWeb.jsp.suffix";

	/**
	 * True if a default WebTemplateRegistry should be created with support
	 * for WebTemplateProcessors.
	 */
	public static final String TEMPLATES_ENABLED = "acrossWeb.templates.enabled";

	/**
	 * True if NamedWebTemplateProcessor instances should automatically register themselves
	 * in the registry. Only relevant if templates are enabled.
	 */
	public static final String TEMPLATES_AUTO_REGISTER = "acrossWeb.templates.autoregister";

	public static final String DEVELOPMENT_VIEWS_PROPERTIES_LOCATION = "acrossWeb.development.views.properties";

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
		registry.register( TEMPLATES_ENABLED, Boolean.class, true );
		registry.register( TEMPLATES_AUTO_REGISTER, Boolean.class, true );
		registry.register( DEVELOPMENT_VIEWS_PROPERTIES_LOCATION, String.class,
		                   "${user.home}/dev-configs/across-devel.properties",
		                   "Location of the properties file containing acrossWeb.views.* entries for development mode." );
	}

	public boolean isTemplatesEnabled() {
		return getProperty( TEMPLATES_ENABLED, Boolean.class );
	}

	public boolean isAutoRegisterTemplates() {
		return getProperty( TEMPLATES_AUTO_REGISTER, Boolean.class );
	}
}
