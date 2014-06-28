package com.foreach.across.core.context;

/**
 * Dependency hints - impact the bootstrapping order of modules.
 */
public enum AcrossModuleRole
{
	APPLICATION,
	INFRASTRUCTURE,
	POSTPROCESSOR
}
