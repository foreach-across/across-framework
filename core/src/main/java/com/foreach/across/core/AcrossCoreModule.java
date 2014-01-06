package com.foreach.across.core;

import com.foreach.across.core.installers.AcrossCoreInstaller;

public class AcrossCoreModule extends AcrossModule
{
	public String getName() {
		return "Across";
	}

	@Override
	public String getDescription() {
		return "Provides the core Across functionality.";
	}

	@Override
	protected Class[] installerClasses() {
		return new Class[] { AcrossCoreInstaller.class };
	}
}
