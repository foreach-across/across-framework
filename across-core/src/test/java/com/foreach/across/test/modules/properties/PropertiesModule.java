package com.foreach.across.test.modules.properties;

import com.foreach.across.core.AcrossModule;

public class PropertiesModule extends AcrossModule
{
	private final String name;

	public PropertiesModule( String name ) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return "Test module for property loading.";
	}
}
