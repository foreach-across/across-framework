package com.foreach.across.test.modules.installer;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.modules.installer.installers.AlwaysRunAfterModuleBootstrapInstaller;
import com.foreach.across.test.modules.installer.installers.AlwaysRunBeforeContextBootstrapInstaller;
import com.foreach.across.test.modules.installer.installers.VersionBasedInstaller;

public class InstallerModule extends AcrossModule
{
	@Override
	public String getName() {
		return "InstallerModule";
	}

	@Override
	public String getDescription() {
		return "Test module for installers.";
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				AlwaysRunBeforeContextBootstrapInstaller.class,
				AlwaysRunAfterModuleBootstrapInstaller.class,
				VersionBasedInstaller.class
		};
	}
}
