package com.foreach.across.core.installers;

import com.foreach.across.core.*;

@Installer(description = "Installs the core Across base configuration.", runCondition = InstallerRunCondition.VersionDifferent,
           phase = InstallerPhase.BeforeContextBootstrap)
public class AcrossCoreInstaller
{
	@InstallerMethod
	protected void install() {
		// Nothing to do
	}
}
