package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerRunCondition;

@AcrossDepends(required = "requiredModule")
@Installer(description = "Installer that will always run if dependency is met.",
           runCondition = InstallerRunCondition.AlwaysRun)
public class AlwaysRunWithDependencyInstaller extends TestInstaller
{
}
