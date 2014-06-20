package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.modules.debugweb.config.DebugWebMvcConfiguration;

import java.util.Set;

@AcrossDepends(required = "AcrossWebModule")
public class DebugWebModule extends AcrossModule
{
	private String rootPath = "/debug";

	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
	}

	public String getRootPath() {
		return rootPath;
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	@Override
	public String getName() {
		return DebugWeb.MODULE;
	}

	@Override
	public String getDescription() {
		return "Provides a debug web path and functionality to easily register additional debug controllers.";
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( DebugWebMvcConfiguration.class ) );
	}
}

