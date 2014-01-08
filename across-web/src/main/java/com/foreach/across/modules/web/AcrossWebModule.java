package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModule;

public class AcrossWebModule extends AcrossModule
{
	@Override
	public String getName() {
		return "AcrossWebModule";
	}

	@Override
	public String getDescription() {
		return "Base Across web functionality based on spring mvc";
	}
}
