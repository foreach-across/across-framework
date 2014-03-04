package com.foreach.across.core.context.configurer;

/**
 * Used on AcrossContext to determine to which level the configurers should be applied.
 */
public enum ConfigurerScope
{
	CONTEXT_ONLY,
	MODULES_ONLY,
	CONTEXT_AND_MODULES
}
