package com.foreach.across.test.modules.exposing;

import com.foreach.across.core.AcrossModule;

public class ExposingModule extends AcrossModule
{
	private String name;

	public ExposingModule( String name ) {
		this.name = name;
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Description of the content of this module.
	 */
	@Override
	public String getDescription() {
		return null;
	}
}

