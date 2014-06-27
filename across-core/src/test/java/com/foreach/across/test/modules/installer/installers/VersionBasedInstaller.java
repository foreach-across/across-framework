package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.installers.InstallerRunCondition;

@Installer(description = "Installer that will run if version is higher.",
           runCondition = InstallerRunCondition.VersionDifferent,
           version = VersionBasedInstaller.VERSION)
@InstallerGroup(VersionBasedInstaller.GROUP)
public class VersionBasedInstaller extends TestInstaller
{
	public static final int VERSION = 3;
	public static final String GROUP = "test-group";
}
