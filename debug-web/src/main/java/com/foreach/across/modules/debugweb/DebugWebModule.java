package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossModule;

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
		return "DebugWebModule";
	}

	@Override
	public String getDescription() {
		return "Provides a debug web path and functionality to easily register additional debug controllers.";
	}
}

