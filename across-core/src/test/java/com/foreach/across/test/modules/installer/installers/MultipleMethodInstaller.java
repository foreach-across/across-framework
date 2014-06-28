package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerRunCondition;

import java.math.BigDecimal;

@Installer(description = "Installer that will always run and has 3 installer methods.",
           runCondition = InstallerRunCondition.AlwaysRun)
public class MultipleMethodInstaller extends TestInstaller
{
	@InstallerMethod
	public void runAlso() {
		// Overrides the parent method
		EXECUTED.add( String.class );
	}

	@InstallerMethod
	public void anotherMethod() {
		EXECUTED.add( Object.class );
	}
}
