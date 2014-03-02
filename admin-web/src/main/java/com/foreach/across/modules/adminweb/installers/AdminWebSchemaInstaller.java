package com.foreach.across.modules.adminweb.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Installs database schema for user authorization.", version = 1)
public class AdminWebSchemaInstaller extends AcrossLiquibaseInstaller
{
}
