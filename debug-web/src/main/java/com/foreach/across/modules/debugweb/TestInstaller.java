package com.foreach.across.modules.debugweb;

import com.foreach.across.core.Installer;
import com.foreach.across.core.InstallerMethod;

@Installer(description = "installing something for debugwebmodule")
public class TestInstaller
{
	@InstallerMethod
	protected void install() {
		System.out.println( "doing installation" );
	}
}
