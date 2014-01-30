package com.foreach.across.testweb.sub;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;

@AcrossDepends(required = "AcrossWebModule")
public class TestWebModule extends AcrossModule
{
	@Override
	public String getName() {
		return "TestWebModule";
	}

	@Override
	public String getDescription() {
		return null;
	}
}
