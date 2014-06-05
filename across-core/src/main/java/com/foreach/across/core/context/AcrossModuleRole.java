package com.foreach.across.core.context;

/**
 * Dependency hints - impact the bootstrapping order of modules.
 */
public enum AcrossModuleRole
{
	CUSTOM,
	INFRASTRUCTURE,
	POSTPROCESSOR
}
