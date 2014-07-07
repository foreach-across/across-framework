package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;

@Installer(description = "Installer that will always run.",
           runCondition = InstallerRunCondition.AlwaysRun,
           phase = InstallerPhase.AfterModuleBootstrap)
public class AlwaysRunAfterModuleBootstrapInstaller extends TestInstaller
{

}
