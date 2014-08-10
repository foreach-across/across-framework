package com.foreach.across.core.installers;

import com.foreach.across.core.annotations.Installer;

@Installer(
		description = "Installs the across_sequences table.  This installer can be used by separate modules and is safe to be"
				+ "executed multiple times by different modules.",
		version = 1
)
public class AcrossSequencesInstaller extends AcrossLiquibaseInstaller
{
}
