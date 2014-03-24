package com.foreach.across.core;

/**
 * Default empty AcrossModule with a configurable name. This module does not do anything by default,
 * but can be used to inject in an AcrossContext to satisfy dependencies.
 */
public final class EmptyAcrossModule extends AcrossModule
{
	private final String name;

	public EmptyAcrossModule( String name ) {
		this.name = name;
	}

	/**
	 * @return Name of this module.  Should be unique within a configured AcrossContext.
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
		return "Standard empty module implementation - configurable by name to allow for faking dependencies.";
	}
}
