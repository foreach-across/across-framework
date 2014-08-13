package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerRunCondition;

@Installer(description = "Installer that will always run.", runCondition = InstallerRunCondition.AlwaysRun)
public class AlwaysRunBeforeContextBootstrapInstaller extends TestInstaller
{
}
