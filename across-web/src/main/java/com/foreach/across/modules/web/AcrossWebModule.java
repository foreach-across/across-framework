package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModule;

public class AcrossWebModule extends AcrossModule
{
	public AcrossWebModule() {
//setExposeBeansToParent( false );
	}

	@Override
	public String getName() {
		return "AcrossWebModule";
	}

	@Override
	public String getDescription() {
		return "Base Across web functionality based on spring mvc";
	}

	@Override
	public void bootstrap() {
		super.bootstrap();
	}

	@Override
	public String[] getComponentScanPackages() {
		return new String[] { "com.foreach.across.modules.web.scan" };
	}
}
