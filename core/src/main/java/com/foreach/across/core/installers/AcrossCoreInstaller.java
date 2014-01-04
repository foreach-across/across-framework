package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossInstaller;

public class AcrossCoreInstaller extends AcrossInstaller
{
	@Override
	public String getDescription() {
		return "Installs the core Across base configuration.";
	}

	@Override
	protected void install() {
		// Nothing to do
		System.out.println("installing core");
	}
}
