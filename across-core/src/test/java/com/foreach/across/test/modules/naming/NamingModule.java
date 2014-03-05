package com.foreach.across.test.modules.naming;

import com.foreach.across.core.AcrossModule;

public class NamingModule extends AcrossModule
{
	private String name;

	public NamingModule( String name ) {
		this.name = name;
	}

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
