package com.foreach.across.modules.debugweb;

import com.foreach.across.core.AcrossInstaller;

public class TestInstaller extends AcrossInstaller
{
	@Override
	public String getDescription() {
		return "installing something for debugwebmodule";
	}

	@Override
	protected void install() {
		System.out.println("doing installation");
	}
}
