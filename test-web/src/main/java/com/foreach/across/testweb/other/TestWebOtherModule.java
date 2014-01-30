package com.foreach.across.testweb.other;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;

@AcrossDepends(required = "AcrossWebModule", optional = "DebugWebModule")
public class TestWebOtherModule extends AcrossModule
{
	@Override
	public String getName() {
		return "TestWebOtherModule";
	}

	@Override
	public String getDescription() {
		return null;
	}
}
